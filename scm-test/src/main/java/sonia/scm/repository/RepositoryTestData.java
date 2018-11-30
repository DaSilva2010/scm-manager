/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
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
 */


package sonia.scm.repository;

public final class RepositoryTestData {

  public static final String NAMESPACE = "hitchhiker";
  public static final String MAIL_DOMAIN = "@hitchhiker.com";

  private RepositoryTestData() {
  }

  public static Repository create42Puzzle() {
    return create42Puzzle(DummyRepositoryHandler.TYPE_NAME);
  }

  public static Repository create42Puzzle(String type) {
    return new RepositoryBuilder()
      .type(type)
      .contact("douglas.adams" + MAIL_DOMAIN)
      .name("42Puzzle")
      .namespace(NAMESPACE)
      .description("The 42 Puzzle")
      .build();
  }

  public static Repository createHappyVerticalPeopleTransporter() {
    return createHappyVerticalPeopleTransporter(
      DummyRepositoryHandler.TYPE_NAME);
  }

  public static Repository createHappyVerticalPeopleTransporter(String type) {
    return new RepositoryBuilder()
      .type(type)
      .contact("zaphod.beeblebrox" + MAIL_DOMAIN)
      .name("happyVerticalPeopleTransporter")
      .namespace(NAMESPACE)
      .description("Happy Vertical People Transporter")
      .build();
  }

  public static Repository createHeartOfGold() {
    return createHeartOfGold(DummyRepositoryHandler.TYPE_NAME);
  }

  public static Repository createHeartOfGold(String type) {
    return new RepositoryBuilder()
      .type(type)
      .contact("zaphod.beeblebrox" + MAIL_DOMAIN)
      .name("HeartOfGold")
      .namespace(NAMESPACE)
      .description(
        "Heart of Gold is the first prototype ship to successfully utilise the revolutionary Infinite Improbability Drive")
      .build();
  }

  public static Repository createRestaurantAtTheEndOfTheUniverse() {
    return createRestaurantAtTheEndOfTheUniverse(
      DummyRepositoryHandler.TYPE_NAME);
  }

  public static Repository createRestaurantAtTheEndOfTheUniverse(String type) {
    return new RepositoryBuilder()
      .type(type)
      .contact("douglas.adams" + MAIL_DOMAIN)
      .name("RestaurantAtTheEndOfTheUniverse")
      .namespace(NAMESPACE)
      .description("The Restaurant at the End of the Universe")
      .build();
  }
}
