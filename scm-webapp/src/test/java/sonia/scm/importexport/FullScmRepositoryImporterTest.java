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

package sonia.scm.importexport;

import com.google.common.io.Resources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.ImportFailedException;
import sonia.scm.repository.api.IncompatibleEnvironmentForImportException;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.api.UnbundleCommandBuilder;
import sonia.scm.repository.work.WorkdirProvider;
import sonia.scm.update.UpdateEngine;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("UnstableApiUsage")
class FullScmRepositoryImporterTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold("svn");

  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private RepositoryService service;
  @Mock
  private UnbundleCommandBuilder unbundleCommandBuilder;
  @Mock
  private RepositoryManager repositoryManager;
  @Mock
  private ScmEnvironmentCompatibilityChecker compatibilityChecker;
  @Mock
  private TarArchiveRepositoryStoreImporter storeImporter;
  @Mock
  private UpdateEngine updateEngine;
  @Mock
  private RepositoryImportExportEncryption repositoryImportExportEncryption;
  @Mock
  private WorkdirProvider workdirProvider;

  @InjectMocks
  private EnvironmentCheckStep environmentCheckStep;
  @InjectMocks
  private MetadataImportStep metadataImportStep;
  @InjectMocks
  private StoreImportStep storeImportStep;
  @InjectMocks
  private RepositoryImportStep repositoryImportStep;

  private FullScmRepositoryImporter fullImporter;

  @BeforeEach
  void initTestObject() {
    fullImporter = new FullScmRepositoryImporter(
      environmentCheckStep,
      metadataImportStep,
      storeImportStep,
      repositoryImportStep,
      repositoryManager,
      repositoryImportExportEncryption);
  }

  @BeforeEach
  void initRepositoryService() {
    lenient().when(serviceFactory.create(REPOSITORY)).thenReturn(service);
    lenient().when(service.getUnbundleCommand()).thenReturn(unbundleCommandBuilder);
  }

  @Test
  void shouldNotImportRepositoryIfFileNotExists(@TempDir Path temp) throws IOException {
    Path emptyFile = temp.resolve("empty");
    Files.createFile(emptyFile);
    FileInputStream inputStream = new FileInputStream(emptyFile.toFile());
    assertThrows(
      ImportFailedException.class,
      () -> fullImporter.importFromStream(REPOSITORY, inputStream, "")
    );
  }

  @Test
  void shouldFailIfScmEnvironmentIsIncompatible() throws IOException {
    when(compatibilityChecker.check(any())).thenReturn(false);

    InputStream importStream = Resources.getResource("sonia/scm/repository/import/scm-import.tar.gz").openStream();
    assertThrows(
      IncompatibleEnvironmentForImportException.class,
      () -> fullImporter.importFromStream(REPOSITORY, importStream, "")
    );
  }

  @Nested
  class WithValidEnvironment {

    @BeforeEach
    void setUpEnvironment(@TempDir Path temp) {
      lenient().when(workdirProvider.createNewWorkdir(REPOSITORY.getId())).thenReturn(temp.toFile());

      when(compatibilityChecker.check(any())).thenReturn(true);
      when(repositoryManager.create(eq(REPOSITORY))).thenReturn(REPOSITORY);
    }

    @Test
    void shouldImportScmRepositoryArchiveWithWorkDir() throws IOException {
      InputStream stream = Resources.getResource("sonia/scm/repository/import/scm-import.tar.gz").openStream();

      Repository repository = fullImporter.importFromStream(REPOSITORY, stream, "");

      assertThat(repository).isEqualTo(REPOSITORY);
      verify(storeImporter).importFromTarArchive(eq(REPOSITORY), any(InputStream.class));
      verify(repositoryManager).modify(REPOSITORY);
      Collection<RepositoryPermission> updatedPermissions = REPOSITORY.getPermissions();
      assertThat(updatedPermissions).hasSize(2);
      verify(unbundleCommandBuilder).unbundle((InputStream) argThat(argument -> argument.getClass().equals(NoneClosingInputStream.class)));
      verify(workdirProvider, times(1)).createNewWorkdir(REPOSITORY.getId());
    }

    @Test
    void shouldNotExistWorkDirAfterRepositoryImportIsFinished(@TempDir Path temp) throws IOException {
      when(workdirProvider.createNewWorkdir(REPOSITORY.getId())).thenReturn(temp.toFile());
      InputStream stream = Resources.getResource("sonia/scm/repository/import/scm-import.tar.gz").openStream();
      fullImporter.importFromStream(REPOSITORY, stream, "");

      boolean workDirExists = Files.exists(temp);
      assertThat(workDirExists).isFalse();
    }

    @Test
    void shouldTriggerUpdateForImportedRepository() throws IOException {
      InputStream stream = Resources.getResource("sonia/scm/repository/import/scm-import.tar.gz").openStream();

      fullImporter.importFromStream(REPOSITORY, stream, "");

      verify(updateEngine).update(REPOSITORY.getId());
    }

    @Test
    void shouldImportRepositoryDirectlyWithoutCopyInWorkDir() throws IOException {
      InputStream stream = Resources.getResource("sonia/scm/repository/import/scm-import-stores-before-repository.tar.gz").openStream();
      Repository repository = fullImporter.importFromStream(REPOSITORY, stream, "");

      assertThat(repository).isEqualTo(REPOSITORY);
      verify(storeImporter).importFromTarArchive(eq(REPOSITORY), any(InputStream.class));
      verify(repositoryManager).modify(REPOSITORY);
      verify(unbundleCommandBuilder).unbundle((InputStream) argThat(argument -> argument.getClass().equals(NoneClosingInputStream.class)));
      verify(workdirProvider, never()).createNewWorkdir(REPOSITORY.getId());
    }

    @Test
    void shouldDecryptStreamWhenPasswordSet() throws IOException {
      InputStream stream = Resources.getResource("sonia/scm/repository/import/scm-import.tar.gz").openStream();
      when(repositoryImportExportEncryption.decrypt(any(), eq("hg2tg"))).thenAnswer(invocation -> invocation.getArgument(0));

      fullImporter.importFromStream(REPOSITORY, stream, "hg2tg");

      verify(updateEngine).update(REPOSITORY.getId());
    }
  }
}
