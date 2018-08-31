package sonia.scm.api.v2.resources;

import com.google.inject.Inject;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import sonia.scm.plugin.PluginWrapper;

import java.util.Collection;
import java.util.List;

import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Links.linkingTo;
import static java.util.stream.Collectors.toList;

public class UIPluginDtoCollectionMapper {

  private final ResourceLinks resourceLinks;
  private final UIPluginDtoMapper mapper;

  @Inject
  public UIPluginDtoCollectionMapper(ResourceLinks resourceLinks, UIPluginDtoMapper mapper) {
    this.resourceLinks = resourceLinks;
    this.mapper = mapper;
  }

  public HalRepresentation map(Collection<PluginWrapper> plugins) {
    List<UIPluginDto> dtos = plugins.stream().map(mapper::map).collect(toList());
    return new HalRepresentation(createLinks(), embedDtos(dtos));
  }

  private Links createLinks() {
    String baseUrl = resourceLinks.uiPluginCollection().self();

    Links.Builder linksBuilder = linkingTo()
      .with(Links.linkingTo().self(baseUrl).build());
    return linksBuilder.build();
  }

  private Embedded embedDtos(List<UIPluginDto> dtos) {
    return embeddedBuilder()
      .with("plugins", dtos)
      .build();
  }

}
