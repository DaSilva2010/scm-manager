package sonia.scm.api.v2.resources;

import com.github.sdorra.ssp.PermissionCheck;
import de.otto.edison.hal.HalRepresentation;
import sonia.scm.AlreadyExistsException;
import sonia.scm.ConcurrentModificationException;
import sonia.scm.Manager;
import sonia.scm.ModelObject;
import sonia.scm.NotFoundException;
import sonia.scm.PageResult;
import sonia.scm.user.User;
import sonia.scm.user.UserPermissions;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.AuthenticationUtil;

import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Facade for {@link SingleResourceManagerAdapter} and {@link CollectionResourceManagerAdapter}
 * for model objects handled by a single id.
 */
@SuppressWarnings("squid:S00119") // "MODEL_OBJECT" is much more meaningful than "M", right?
class IdResourceManagerAdapter<MODEL_OBJECT extends ModelObject,
                             DTO extends HalRepresentation> {

  private final Manager<MODEL_OBJECT> manager;

  private final SingleResourceManagerAdapter<MODEL_OBJECT, DTO> singleAdapter;
  private final CollectionResourceManagerAdapter<MODEL_OBJECT, DTO> collectionAdapter;

  IdResourceManagerAdapter(Manager<MODEL_OBJECT> manager, Class<MODEL_OBJECT> type) {
    this.manager = manager;
    singleAdapter = new SingleResourceManagerAdapter<>(manager, type);
    collectionAdapter = new CollectionResourceManagerAdapter<>(manager, type);
  }

  Response get(String id, Function<MODEL_OBJECT, DTO> mapToDto) throws NotFoundException {
    return singleAdapter.get(loadBy(id), mapToDto);
  }


  /**
   * If the authenticated user is the same user that want to change password than return the changeOwnPassword verification function
   * if the authenticated user is different he should have the modify permission to be able to modify passwords of other users
   *
   * @param usernameToChangePassword the user name of the user we want to change password
   * @return function to verify permission
   */
  public Function<MODEL_OBJECT, PermissionCheck> getChangePasswordPermission(String usernameToChangePassword) {
    AssertUtil.assertIsNotEmpty(usernameToChangePassword);
    return model -> {
      User user = (User) model;
      if (usernameToChangePassword.equals(AuthenticationUtil.getAuthenticatedUsername())) {
        return UserPermissions.changeOwnPassword();
      }
      return UserPermissions.modify(user);
    };
  }

  public Response changePassword(String id, Function<MODEL_OBJECT, MODEL_OBJECT> applyChanges, Consumer<MODEL_OBJECT> checker ) throws NotFoundException, ConcurrentModificationException {
    return singleAdapter.changePassword(
      loadBy(id),
      applyChanges,
      idStaysTheSame(id),
      checker,
      getChangePasswordPermission(id));
  }

  public Response update(String id, Function<MODEL_OBJECT, MODEL_OBJECT> applyChanges) throws NotFoundException, ConcurrentModificationException {
    return singleAdapter.update(
      loadBy(id),
      applyChanges,
      idStaysTheSame(id)
    );
  }

  public Response getAll(int page, int pageSize, String sortBy, boolean desc, Function<PageResult<MODEL_OBJECT>, CollectionDto> mapToDto) {
    return collectionAdapter.getAll(page, pageSize, sortBy, desc, mapToDto);
  }

  public Response create(DTO dto, Supplier<MODEL_OBJECT> modelObjectSupplier, Function<MODEL_OBJECT, String> uriCreator) throws AlreadyExistsException {
    return collectionAdapter.create(dto, modelObjectSupplier, uriCreator);
  }

  public Response delete(String id) {
    return singleAdapter.delete(id);
  }

  private Supplier<Optional<MODEL_OBJECT>> loadBy(String id) {
    return () -> Optional.ofNullable(manager.get(id));
  }

  private Predicate<MODEL_OBJECT> idStaysTheSame(String id) {
    return changed -> changed.getId().equals(id);
  }
}
