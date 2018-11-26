/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */


package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.GenericDAO;

import java.io.File;

/**
 * Data access object for repositories. This class should only used by the
 * {@link RepositoryManager}. Plugins and other classes should use the
 * {@link RepositoryManager} instead.
 *
 * @author Sebastian Sdorra
 * @since 1.14
 */
public interface RepositoryDAO extends GenericDAO<Repository>
{

  /**
   * Returns true if a repository with specified
   * namespace and name exists in the backend.
   *
   *
   * @param namespaceAndName namespace and name of the repository
   *
   * @return true if the repository exists
   */
  boolean contains(NamespaceAndName namespaceAndName);

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the repository with the specified namespace and name or null
   * if no such repository exists in the backend.
   *
   * @return repository with the specified namespace and name or null
   */
  Repository get(NamespaceAndName namespaceAndName);
}
