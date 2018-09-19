package sonia.scm.api.v2.resources;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.util.ThreadState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import sonia.scm.repository.Compatibility;
import sonia.scm.repository.SvnConfig;

import java.io.File;
import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SvnConfigToSvnConfigDtoMapperTest {

  private URI baseUri = URI.create("http://example.com/base/");

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ScmPathInfoStore scmPathInfoStore;

  @InjectMocks
  private SvnConfigToSvnConfigDtoMapperImpl mapper;

  private final Subject subject = mock(Subject.class);
  private final ThreadState subjectThreadState = new SubjectThreadState(subject);

  private URI expectedBaseUri;

  @Before
  public void init() {
    when(scmPathInfoStore.get().getApiRestUri()).thenReturn(baseUri);
    expectedBaseUri = baseUri.resolve(SvnConfigResource.SVN_CONFIG_PATH_V2);
    subjectThreadState.bind();
    ThreadContext.bind(subject);
  }

  @After
  public void unbindSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  public void shouldMapFields() {
    SvnConfig config = createConfiguration();

    when(subject.isPermitted("configuration:write:svn")).thenReturn(true);
    SvnConfigDto dto = mapper.map(config);

    assertTrue(dto.isDisabled());
    assertEquals("repository/directory", dto.getRepositoryDirectory().getPath());

    assertEquals(Compatibility.PRE15, dto.getCompatibility());
    assertTrue(dto.isEnabledGZip());

    assertEquals(expectedBaseUri.toString(), dto.getLinks().getLinkBy("self").get().getHref());
    assertEquals(expectedBaseUri.toString(), dto.getLinks().getLinkBy("update").get().getHref());
  }

  @Test
  public void shouldMapFieldsWithoutUpdate() {
    SvnConfig config = createConfiguration();

    when(subject.isPermitted("configuration:write:svn")).thenReturn(false);
    SvnConfigDto dto = mapper.map(config);

    assertEquals(expectedBaseUri.toString(), dto.getLinks().getLinkBy("self").get().getHref());
    assertFalse(dto.getLinks().hasLink("update"));
  }

  private SvnConfig createConfiguration() {
    SvnConfig config = new SvnConfig();
    config.setDisabled(true);
    config.setRepositoryDirectory(new File("repository/directory"));

    config.setCompatibility(Compatibility.PRE15);
    config.setEnabledGZip(true);

    return config;
  }

}
