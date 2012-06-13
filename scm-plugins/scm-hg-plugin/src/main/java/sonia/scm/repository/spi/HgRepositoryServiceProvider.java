/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.ImmutableSet;
import com.google.inject.Provider;

import sonia.scm.repository.HgContext;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.Command;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
public class HgRepositoryServiceProvider extends RepositoryServiceProvider
{

  /** Field description */
  private static final Set<Command> COMMANDS = ImmutableSet.of(Command.BROWSE,
                                                 Command.CAT, Command.DIFF);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   * @param hgContextProvider
   * @param handler
   * @param repository
   */
  HgRepositoryServiceProvider(HgRepositoryHandler handler,
                              Provider<HgContext> hgContextProvider,
                              Repository repository)
  {
    this.hgContextProvider = hgContextProvider;
    this.handler = handler;
    this.repository = repository;
    this.repositoryDirectory = handler.getDirectory(repository);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public HgBrowseCommand getBrowseCommand()
  {
    return new HgBrowseCommand(handler, hgContextProvider.get(), repository,
                               repositoryDirectory);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public HgCatCommand getCatCommand()
  {
    return new HgCatCommand(handler, hgContextProvider.get(), repository,
                            repositoryDirectory);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public HgDiffCommand getDiffCommand()
  {
    return new HgDiffCommand(handler, hgContextProvider.get(), repository,
                             repositoryDirectory);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Set<Command> getSupportedCommands()
  {
    return COMMANDS;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private HgRepositoryHandler handler;

  /** Field description */
  private Provider<HgContext> hgContextProvider;

  /** Field description */
  private Repository repository;

  /** Field description */
  private File repositoryDirectory;
}
