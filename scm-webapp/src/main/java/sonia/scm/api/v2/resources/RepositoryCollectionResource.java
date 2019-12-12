package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.ResponseHeader;
import com.webcohesion.enunciate.metadata.rs.ResponseHeaders;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import org.apache.shiro.SecurityUtils;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.search.SearchRequest;
import sonia.scm.search.SearchUtil;
import sonia.scm.user.User;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import java.util.function.Predicate;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Collections.singletonList;

public class RepositoryCollectionResource {

  private static final int DEFAULT_PAGE_SIZE = 10;

  private final CollectionResourceManagerAdapter<Repository, RepositoryDto> adapter;
  private final RepositoryCollectionToDtoMapper repositoryCollectionToDtoMapper;
  private final RepositoryDtoToRepositoryMapper dtoToRepositoryMapper;
  private final ResourceLinks resourceLinks;

  @Inject
  public RepositoryCollectionResource(RepositoryManager manager, RepositoryCollectionToDtoMapper repositoryCollectionToDtoMapper, RepositoryDtoToRepositoryMapper dtoToRepositoryMapper, ResourceLinks resourceLinks) {
    this.adapter = new CollectionResourceManagerAdapter<>(manager, Repository.class);
    this.repositoryCollectionToDtoMapper = repositoryCollectionToDtoMapper;
    this.dtoToRepositoryMapper = dtoToRepositoryMapper;
    this.resourceLinks = resourceLinks;
  }

  /**
   * Returns all repositories for a given page number with a given page size (default page size is {@value DEFAULT_PAGE_SIZE}).
   *
   * <strong>Note:</strong> This method requires "repository" privilege.
   *
   * @param page     the number of the requested page
   * @param pageSize the page size (default page size is {@value DEFAULT_PAGE_SIZE})
   * @param sortBy   sort parameter (if empty - undefined sorting)
   * @param desc     sort direction desc or asc
   */
  @GET
  @Path("")
  @Produces(VndMediaType.REPOSITORY_COLLECTION)
  @TypeHint(CollectionDto.class)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"repository\" privilege"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response getAll(@DefaultValue("0") @QueryParam("page") int page,
    @DefaultValue("" + DEFAULT_PAGE_SIZE) @QueryParam("pageSize") int pageSize,
    @QueryParam("sortBy") String sortBy,
    @DefaultValue("false") @QueryParam("desc") boolean desc,
    @DefaultValue("") @QueryParam("q") String search
  ) {
    return adapter.getAll(page, pageSize, createSearchPredicate(search), sortBy, desc,
      pageResult -> repositoryCollectionToDtoMapper.map(page, pageSize, pageResult));
  }

  /**
   * Creates a new repository.
   *
   * <strong>Note:</strong> This method requires "repository" privilege. The namespace of the given repository will
   *   be ignored and set by the configured namespace strategy.
   *
   * @param repository The repository to be created.
   * @return A response with the link to the new repository (if created successfully).
   */
  @POST
  @Path("")
  @Consumes(VndMediaType.REPOSITORY)
  @StatusCodes({
    @ResponseCode(code = 201, condition = "create success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"repository\" privilege"),
    @ResponseCode(code = 409, condition = "conflict, a repository with this name already exists"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  @ResponseHeaders(@ResponseHeader(name = "Location", description = "uri to the created repository"))
  public Response create(@Valid RepositoryDto repository) {
    return adapter.create(repository,
      () -> createModelObjectFromDto(repository),
      r -> resourceLinks.repository().self(r.getNamespace(), r.getName()));
  }

  private Repository createModelObjectFromDto(@Valid RepositoryDto repositoryDto) {
    Repository repository = dtoToRepositoryMapper.map(repositoryDto, null);
    repository.setPermissions(singletonList(new RepositoryPermission(currentUser(), "OWNER", false)));
    return repository;
  }

  private String currentUser() {
    return SecurityUtils.getSubject().getPrincipals().oneByType(User.class).getName();
  }

  private Predicate<Repository> createSearchPredicate(String search) {
    if (isNullOrEmpty(search)) {
      return user -> true;
    }
    SearchRequest searchRequest = new SearchRequest(search, true);
    return repository -> SearchUtil.matchesOne(searchRequest, repository.getName(), repository.getNamespace(), repository.getDescription());
  }
}