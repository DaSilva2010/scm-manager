/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
    
package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static com.google.common.collect.ImmutableSet.of;
import static org.hamcrest.Matchers.*;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Sebastian Sdorra
 */
public class PluginTreeTest
{

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test(expected = PluginConditionFailedException.class)
  public void testPluginConditionFailed() throws IOException
  {
    PluginCondition condition = new PluginCondition("999",
                                  new ArrayList<String>(), "hit");
    InstalledPluginDescriptor plugin = new InstalledPluginDescriptor(2, createInfo("a",  "1"), null, condition,
                      false, null, null);
    ExplodedSmp smp = createSmp(plugin);

    new PluginTree(smp).getLeafLastNodes();
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test(expected = PluginNotInstalledException.class)
  public void testPluginNotInstalled() throws IOException
  {
    new PluginTree(createSmpWithDependency("b", "a")).getLeafLastNodes();
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testNodes() throws IOException
  {
    List<ExplodedSmp> smps = createSmps("a", "b", "c");
    List<String> nodes = unwrapIds(new PluginTree(smps).getLeafLastNodes());

    assertThat(nodes, containsInAnyOrder("a", "b", "c"));
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test(expected = PluginException.class)
  public void testScmVersion() throws IOException
  {
    InstalledPluginDescriptor plugin = new InstalledPluginDescriptor(1, createInfo("a", "1"), null, null, false,
                      null, null);
    ExplodedSmp smp = createSmp(plugin);

    new PluginTree(smp).getLeafLastNodes();
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testSimpleDependencies() throws IOException
  {
    //J-
    ExplodedSmp[] smps = new ExplodedSmp[] {
      createSmpWithDependency("a"),
      createSmpWithDependency("b", "a"),
      createSmpWithDependency("c", "a", "b")
    };
    //J+

    PluginTree tree = new PluginTree(smps);
    List<PluginNode> nodes = tree.getLeafLastNodes();

    System.out.println(tree);

    assertThat(unwrapIds(nodes), contains("a", "b", "c"));
  }

  @Test
  public void testComplexDependencies() throws IOException
  {
    //J-
    ExplodedSmp[] smps = new ExplodedSmp[]{
      createSmpWithDependency("a", "b", "c", "d"),
      createSmpWithDependency("b", "c"),
      createSmpWithDependency("c"),
      createSmpWithDependency("d")
    };
    //J+

    PluginTree tree = new PluginTree(smps);
    List<PluginNode> nodes = tree.getLeafLastNodes();

    System.out.println(tree);

    assertThat(unwrapIds(nodes), contains("d", "c", "b", "a"));
  }

  @Test
  public void testWithOptionalDependency() throws IOException {
    ExplodedSmp[] smps = new ExplodedSmp[] {
      createSmpWithDependency("a"),
      createSmpWithDependency("b", null, of("a")),
      createSmpWithDependency("c", null, of("a", "b"))
    };

    PluginTree tree = new PluginTree(smps);
    List<PluginNode> nodes = tree.getLeafLastNodes();

    System.out.println(tree);

    assertThat(unwrapIds(nodes), contains("a", "b", "c"));
  }

  @Test
  public void testRealWorldDependencies() throws IOException {
    //J-
    ExplodedSmp[] smps = new ExplodedSmp[]{
      createSmpWithDependency("scm-editor-plugin"),
      createSmpWithDependency("scm-ci-plugin"),
      createSmpWithDependency("scm-jenkins-plugin"),
      createSmpWithDependency("scm-issuetracker-plugin"),
      createSmpWithDependency("scm-hg-plugin"),
      createSmpWithDependency("scm-git-plugin"),
      createSmpWithDependency("scm-directfilelink-plugin"),
      createSmpWithDependency("scm-ssh-plugin"),
      createSmpWithDependency("scm-pushlog-plugin"),
      createSmpWithDependency("scm-cas-plugin"),
      createSmpWithDependency("scm-tagprotection-plugin"),
      createSmpWithDependency("scm-groupmanager-plugin"),
      createSmpWithDependency("scm-webhook-plugin"),
      createSmpWithDependency("scm-svn-plugin"),
      createSmpWithDependency("scm-support-plugin"),
      createSmpWithDependency("scm-statistic-plugin"),
      createSmpWithDependency("scm-rest-legacy-plugin"),
      createSmpWithDependency("scm-gravatar-plugin"),
      createSmpWithDependency("scm-legacy-plugin"),
      createSmpWithDependency("scm-authormapping-plugin"),
      createSmpWithDependency("scm-readme-plugin"),
      createSmpWithDependency("scm-script-plugin"),
      createSmpWithDependency("scm-activity-plugin"),
      createSmpWithDependency("scm-mail-plugin"),
      createSmpWithDependency("scm-branchwp-plugin", of(), of("scm-editor-plugin", "scm-review-plugin", "scm-mail-plugin" )),
      createSmpWithDependency("scm-notify-plugin", "scm-mail-plugin"),
      createSmpWithDependency("scm-redmine-plugin", "scm-issuetracker-plugin"),
      createSmpWithDependency("scm-jira-plugin", "scm-mail-plugin", "scm-issuetracker-plugin"),
      createSmpWithDependency("scm-review-plugin", of("scm-mail-plugin"), of("scm-editor-plugin")),
      createSmpWithDependency("scm-pathwp-plugin", of(), of("scm-editor-plugin")),
      createSmpWithDependency("scm-cockpit-legacy-plugin", "scm-statistic-plugin", "scm-rest-legacy-plugin", "scm-activity-plugin")
    };
    //J+

    Arrays.stream(smps)
      .forEach(smp -> System.out.println(smp.getPlugin()));


    PluginTree tree = new PluginTree(smps);
    List<PluginNode> nodes = tree.getLeafLastNodes();

    System.out.println(tree);

    assertEachParentHasChild(nodes, "scm-review-plugin", "scm-branchwp-plugin");
  }

  private void assertEachParentHasChild(List<PluginNode> nodes, String parentName, String childName) {
    nodes.forEach(node -> assertEachParentHasChild(node, parentName, childName));
  }

  private void assertEachParentHasChild(PluginNode pluginNode, String parentName, String childName) {
    if (pluginNode.getId().equals(parentName)) {
      assertThat(pluginNode.getChildren().stream().map(PluginNode::getId).collect(Collectors.toList()), contains(childName));
    }
    assertEachParentHasChild(pluginNode.getChildren(), parentName, childName);
  }


  @Test
  public void testWithDeepOptionalDependency() throws IOException {
    ExplodedSmp[] smps = new ExplodedSmp[] {
      createSmpWithDependency("a"),
      createSmpWithDependency("b", "a"),
      createSmpWithDependency("c", null, of("b"))
    };

    PluginTree tree = new PluginTree(smps);

    System.out.println(tree);

    List<PluginNode> nodes = tree.getLeafLastNodes();

    assertThat(unwrapIds(nodes), contains("a", "b", "c"));
  }

  @Test
  public void testWithNonExistentOptionalDependency() throws IOException {
    ExplodedSmp[] smps = new ExplodedSmp[] {
      createSmpWithDependency("a", null, of("b"))
    };

    PluginTree tree = new PluginTree(smps);
    List<PluginNode> nodes = tree.getLeafLastNodes();

    assertThat(unwrapIds(nodes), containsInAnyOrder("a"));
  }

  /**
   * Method description
   *
   *
   * @param name
   * @param version
   *
   * @return
   */
  private PluginInformation createInfo(String name,
    String version)
  {
    PluginInformation info = new PluginInformation();

    info.setName(name);
    info.setVersion(version);

    return info;
  }

  /**
   * Method description
   *
   *
   * @param plugin
   *
   * @return
   *
   * @throws IOException
   */
  private ExplodedSmp createSmp(InstalledPluginDescriptor plugin) throws IOException
  {
    return new ExplodedSmp(tempFolder.newFile().toPath(), plugin);
  }

  /**
   * Method description
   *
   *
   * @param name
   *
   * @return
   *
   * @throws IOException
   */
  private ExplodedSmp createSmp(String name) throws IOException
  {
    return createSmp(new InstalledPluginDescriptor(2, createInfo(name, "1.0.0"), null, null,
      false, null, null));
  }

  /**
   * Method description
   *
   *
   * @param name
   * @param dependencies
   *
   * @return
   *
   * @throws IOException
   */
  private ExplodedSmp createSmpWithDependency(String name,
    String... dependencies)
    throws IOException
  {
    Set<String> dependencySet = new HashSet<>();

    for (String d : dependencies)
    {
      dependencySet.add(d);
    }
    return createSmpWithDependency(name, dependencySet, null);
  }

  private ExplodedSmp createSmpWithDependency(String name, Set<String> dependencies, Set<String> optionalDependencies) throws IOException {
    InstalledPluginDescriptor plugin = new InstalledPluginDescriptor(
      2,
      createInfo(name, "1"),
      null,
      null,
      false,
      dependencies,
      optionalDependencies
    );
    return createSmp(plugin);
  }

  /**
   * Method description
   *
   *
   * @param names
   *
   * @return
   *
   * @throws IOException
   */
  private List<ExplodedSmp> createSmps(String... names) throws IOException
  {
    List<ExplodedSmp> smps = Lists.newArrayList();

    for (String name : names)
    {
      smps.add(createSmp(name));
    }

    return smps;
  }

  /**
   * Method description
   *
   *
   * @param nodes
   *
   * @return
   */
  private List<String> unwrapIds(List<PluginNode> nodes)
  {
    return Lists.transform(nodes, new Function<PluginNode, String>()
    {

      @Override
      public String apply(PluginNode input)
      {
        return input.getId();
      }
    });
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();
}
