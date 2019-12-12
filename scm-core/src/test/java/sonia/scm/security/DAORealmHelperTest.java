package sonia.scm.security;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.PrincipalCollection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.group.GroupDAO;
import sonia.scm.user.User;
import sonia.scm.user.UserDAO;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DAORealmHelperTest {

  @Mock
  private LoginAttemptHandler loginAttemptHandler;

  @Mock
  private UserDAO userDAO;

  @Mock
  private GroupDAO groupDAO;

  private DAORealmHelper helper;

  @BeforeEach
  void setUpObjectUnderTest() {
    helper = new DAORealmHelper(loginAttemptHandler, userDAO, "hitchhiker");
  }

  @Test
  void shouldThrowExceptionWithoutUsername() {
    assertThrows(IllegalArgumentException.class, () -> helper.authenticationInfoBuilder(null).build());
  }

  @Test
  void shouldThrowExceptionWithEmptyUsername() {
    assertThrows(IllegalArgumentException.class, () -> helper.authenticationInfoBuilder("").build());
  }

  @Test
  void shouldThrowExceptionWithUnknownUser() {
    assertThrows(UnknownAccountException.class, () -> helper.authenticationInfoBuilder("trillian").build());
  }

  @Test
  void shouldThrowExceptionOnDisabledAccount() {
    User user = new User("trillian");
    user.setActive(false);
    when(userDAO.get("trillian")).thenReturn(user);

    assertThrows(DisabledAccountException.class, () -> helper.authenticationInfoBuilder("trillian").build());
  }

  @Test
  void shouldReturnAuthenticationInfo() {
    User user = new User("trillian");
    when(userDAO.get("trillian")).thenReturn(user);

    AuthenticationInfo authenticationInfo = helper.authenticationInfoBuilder("trillian").build();
    PrincipalCollection principals = authenticationInfo.getPrincipals();
    assertThat(principals.oneByType(User.class)).isSameAs(user);
    assertThat(principals.oneByType(Scope.class)).isEmpty();
  }

  @Test
  void shouldReturnAuthenticationInfoWithScope() {
    User user = new User("trillian");
    when(userDAO.get("trillian")).thenReturn(user);

    Scope scope = Scope.valueOf("user:*", "group:*");

    AuthenticationInfo authenticationInfo = helper.authenticationInfoBuilder("trillian")
      .withScope(scope)
      .build();

    PrincipalCollection principals = authenticationInfo.getPrincipals();
    assertThat(principals.oneByType(Scope.class)).isSameAs(scope);
  }

  @Test
  void shouldReturnAuthenticationInfoWithCredentials() {
    User user = new User("trillian");
    when(userDAO.get("trillian")).thenReturn(user);

    AuthenticationInfo authenticationInfo = helper.authenticationInfoBuilder("trillian")
      .withCredentials("secret")
      .build();

    assertThat(authenticationInfo.getCredentials()).isEqualTo("secret");
  }

  @Test
  void shouldReturnAuthenticationInfoWithCredentialsFromUser() {
    User user = new User("trillian");
    user.setPassword("secret");
    when(userDAO.get("trillian")).thenReturn(user);

    AuthenticationInfo authenticationInfo = helper.authenticationInfoBuilder("trillian").build();

    assertThat(authenticationInfo.getCredentials()).isEqualTo("secret");
  }

  @Test
  void shouldThrowExceptionWithWrongTypeOfToken() {
    assertThrows(IllegalArgumentException.class, () -> helper.getAuthenticationInfo(BearerToken.valueOf("__bearer__")));
  }

  @Test
  void shouldGetAuthenticationInfo() {
    User user = new User("trillian");
    when(userDAO.get("trillian")).thenReturn(user);

    AuthenticationInfo authenticationInfo = helper.getAuthenticationInfo(new UsernamePasswordToken("trillian", "secret"));

    PrincipalCollection principals = authenticationInfo.getPrincipals();
    assertThat(principals.oneByType(User.class)).isSameAs(user);
    assertThat(principals.oneByType(Scope.class)).isEmpty();

    assertThat(authenticationInfo.getCredentials()).isNull();
  }
}