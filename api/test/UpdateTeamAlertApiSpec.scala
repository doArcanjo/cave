import com.cave.metrics.data._
import data.UserData
import org.joda.time.DateTime
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.{Matchers, Mockito}
import play.api.libs.json.Json
import play.api.mvc.Results
import play.api.test._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class UpdateTeamAlertApiSpec extends PlaySpecification with Results with AbstractAlertApiSpec with UserData {

  "PUT /organizations/:name/teams/:team/alerts/:id" should {

    "respond with 200 and the updated alert" in running(FakeApplication(withGlobal = mockGlobal)) {
      Mockito.reset(mockAwsWrapper, mockDataManager, mockInfluxClientFactory)
      when(mockDataManager.findUserByToken(Matchers.eq(SOME_TOKEN), any[DateTime])
        (any[ExecutionContext])).thenReturn(Future.successful(Some(SOME_USER)))
      when(mockDataManager.getOrganization(GiltName)).thenReturn(Success(Some(GiltOrg)))
      when(mockDataManager.getTeam(GiltOrg, GiltTeamName)).thenReturn(Success(Some(GiltTeam)))
      when(mockDataManager.getTeamsForUser(GiltOrg, SOME_USER)).thenReturn(Future.successful(List(GiltTeamName -> Role.Member)))
      when(mockAlertManager.getAlert(AlertId)).thenReturn(Success(Some(SomeAlert)))
      when(mockAlertManager.updateAlert(any[Alert], any[AlertPatch])).thenReturn(Success(Some(SomeOtherAlert)))
      when(mockAwsWrapper.updateAlertNotification(any[Schedule])).thenReturn(Future.successful())

      val result = new TestController().updateTeamAlert(GiltName, GiltTeamName, AlertId)(
        FakeRequest(PUT, s"/organizations/$GiltName/teams/$GiltTeamName/alerts",
          FakeHeaders(Seq(AUTHORIZATION -> Seq(AUTH_TOKEN))),
          SomeOtherAlertJson))

      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsString(result) must equalTo(Json.stringify(SomeOtherAlertJson))
    }

    "also work if the user is an org admin" in running(FakeApplication(withGlobal = mockGlobal)) {
      Mockito.reset(mockAwsWrapper, mockDataManager, mockInfluxClientFactory)
      when(mockDataManager.findUserByToken(Matchers.eq(SOME_TOKEN), any[DateTime])
        (any[ExecutionContext])).thenReturn(Future.successful(Some(SOME_USER)))
      when(mockDataManager.getOrganization(GiltName)).thenReturn(Success(Some(GiltOrg)))
      when(mockDataManager.getTeam(GiltOrg, GiltTeamName)).thenReturn(Success(Some(GiltTeam)))
      when(mockDataManager.getTeamsForUser(GiltOrg, SOME_USER)).thenReturn(Future.successful(List(GiltTeamName -> Role.Viewer)))
      when(mockDataManager.getOrganizationsForUser(SOME_USER)).thenReturn(Future.successful(List(GiltName -> Role.Admin)))
      when(mockAlertManager.getAlert(AlertId)).thenReturn(Success(Some(SomeAlert)))
      when(mockAlertManager.updateAlert(any[Alert], any[AlertPatch])).thenReturn(Success(Some(SomeOtherAlert)))
      when(mockAwsWrapper.updateAlertNotification(any[Schedule])).thenReturn(Future.successful())

      val result = new TestController().updateTeamAlert(GiltName, GiltTeamName, AlertId)(
        FakeRequest(PUT, s"/organizations/$GiltName/teams/$GiltTeamName/alerts",
          FakeHeaders(Seq(AUTHORIZATION -> Seq(AUTH_TOKEN))),
          SomeOtherAlertJson))

      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsString(result) must equalTo(Json.stringify(SomeOtherAlertJson))
    }

    "respond with 404 if the organization is not found" in running(FakeApplication(withGlobal = mockGlobal)) {

      Mockito.reset(mockAwsWrapper, mockDataManager, mockInfluxClientFactory)
      when(mockDataManager.findUserByToken(Matchers.eq(SOME_TOKEN), any[DateTime])
        (any[ExecutionContext])).thenReturn(Future.successful(Some(SOME_USER)))
      when(mockDataManager.getOrganization(GiltName)).thenReturn(Success(None))

      val result = new TestController().updateTeamAlert(GiltName, GiltTeamName, AlertId)(
        FakeRequest(PUT, s"/organizations/$GiltName/teams/$GiltTeamName/alerts",
          FakeHeaders(Seq(AUTHORIZATION -> Seq(AUTH_TOKEN))),
          SomeOtherAlertJson))

      verify(mockAwsWrapper, never()).updateAlertNotification(any[Schedule])
      status(result) must equalTo(NOT_FOUND)
      contentAsString(result) must equalTo("")
    }

    "respond with 404 if the team is not found" in running(FakeApplication(withGlobal = mockGlobal)) {

      Mockito.reset(mockAwsWrapper, mockDataManager, mockInfluxClientFactory)
      when(mockDataManager.findUserByToken(Matchers.eq(SOME_TOKEN), any[DateTime])
        (any[ExecutionContext])).thenReturn(Future.successful(Some(SOME_USER)))
      when(mockDataManager.getOrganization(GiltName)).thenReturn(Success(Some(GiltOrg)))
      when(mockDataManager.getTeam(GiltOrg, GiltTeamName)).thenReturn(Success(None))

      val result = new TestController().updateTeamAlert(GiltName, GiltTeamName, AlertId)(
        FakeRequest(PUT, s"/organizations/$GiltName/teams/$GiltTeamName/alerts",
          FakeHeaders(Seq(AUTHORIZATION -> Seq(AUTH_TOKEN))),
          SomeOtherAlertJson))

      verify(mockAwsWrapper, never()).updateAlertNotification(any[Schedule])
      status(result) must equalTo(NOT_FOUND)
      contentAsString(result) must equalTo("")
    }

    "respond with 404 if the alert is not found" in running(FakeApplication(withGlobal = mockGlobal)) {

      Mockito.reset(mockAwsWrapper, mockDataManager, mockInfluxClientFactory)
      when(mockDataManager.findUserByToken(Matchers.eq(SOME_TOKEN), any[DateTime])
        (any[ExecutionContext])).thenReturn(Future.successful(Some(SOME_USER)))
      when(mockDataManager.getOrganization(GiltName)).thenReturn(Success(Some(GiltOrg)))
      when(mockDataManager.getTeam(GiltOrg, GiltTeamName)).thenReturn(Success(Some(GiltTeam)))
      when(mockDataManager.getTeamsForUser(GiltOrg, SOME_USER)).thenReturn(Future.successful(List(GiltTeamName -> Role.Member)))
      when(mockAlertManager.getAlert(AlertId)).thenReturn(Success(None))

      val result = new TestController().updateTeamAlert(GiltName, GiltTeamName, AlertId)(
        FakeRequest(PUT, s"/organizations/$GiltName/teams/$GiltTeamName/alerts",
          FakeHeaders(Seq(AUTHORIZATION -> Seq(AUTH_TOKEN))),
          SomeOtherAlertJson))

      verify(mockAwsWrapper, never()).updateAlertNotification(any[Schedule])
      status(result) must equalTo(NOT_FOUND)
      contentAsString(result) must equalTo("")
    }

    "respond with 404 if the alert is deleted" in running(FakeApplication(withGlobal = mockGlobal)) {

      Mockito.reset(mockAwsWrapper, mockDataManager, mockInfluxClientFactory)
      when(mockDataManager.findUserByToken(Matchers.eq(SOME_TOKEN), any[DateTime])
        (any[ExecutionContext])).thenReturn(Future.successful(Some(SOME_USER)))
      when(mockDataManager.getOrganization(GiltName)).thenReturn(Success(Some(GiltOrg)))
      when(mockDataManager.getTeam(GiltOrg, GiltTeamName)).thenReturn(Success(Some(GiltTeam)))
      when(mockDataManager.getTeamsForUser(GiltOrg, SOME_USER)).thenReturn(Future.successful(List(GiltTeamName -> Role.Member)))
      when(mockAlertManager.getAlert(AlertId)).thenReturn(Success(Some(SomeAlert)))
      when(mockAlertManager.updateAlert(any[Alert], any[AlertPatch])).thenReturn(Success(None))

      val result = new TestController().updateTeamAlert(GiltName, GiltTeamName, AlertId)(
        FakeRequest(PUT, s"/organizations/$GiltName/teams/$GiltTeamName/alerts",
          FakeHeaders(Seq(AUTHORIZATION -> Seq(AUTH_TOKEN))),
          SomeOtherAlertJson))

      verify(mockAwsWrapper, never()).updateAlertNotification(any[Schedule])
      status(result) must equalTo(NOT_FOUND)
      contentAsString(result) must equalTo("")
    }

    "respond with 401 if no credentials are specified" in running(FakeApplication(withGlobal = mockGlobal)) {

      Mockito.reset(mockAwsWrapper, mockDataManager, mockInfluxClientFactory)

      val result = new TestController().updateTeamAlert(GiltName, GiltTeamName, AlertId)(
        FakeRequest(PUT, s"/organizations/$GiltName/teams/$GiltTeamName/alerts",
          FakeHeaders(),
          SomeOtherAlertJson))

      verify(mockAwsWrapper, never()).updateAlertNotification(any[Schedule])
      status(result) must equalTo(UNAUTHORIZED)
      contentAsString(result) must equalTo("")
    }

    "respond with 401 if unsupported credentials are specified" in running(FakeApplication(withGlobal = mockGlobal)) {

      Mockito.reset(mockAwsWrapper, mockDataManager, mockInfluxClientFactory)

      val result = new TestController().updateTeamAlert(GiltName, GiltTeamName, AlertId)(
        FakeRequest(PUT, s"/organizations/$GiltName/teams/$GiltTeamName/alerts",
          FakeHeaders(Seq(AUTHORIZATION -> Seq(GiltTeamBadAuth))),
          SomeOtherAlertJson))

      verify(mockAwsWrapper, never()).updateAlertNotification(any[Schedule])
      status(result) must equalTo(UNAUTHORIZED)
      contentAsString(result) must equalTo("")
    }

    "respond with 403 if user not found" in running(FakeApplication(withGlobal = mockGlobal)) {

      Mockito.reset(mockAwsWrapper, mockDataManager, mockInfluxClientFactory)
      when(mockDataManager.findUserByToken(Matchers.eq(SOME_TOKEN), any[DateTime])
        (any[ExecutionContext])).thenReturn(Future.successful(None))

      val result = new TestController().updateTeamAlert(GiltName, GiltTeamName, AlertId)(
        FakeRequest(PUT, s"/organizations/$GiltName/teams/$GiltTeamName/alerts",
          FakeHeaders(Seq(AUTHORIZATION -> Seq(AUTH_TOKEN))),
          SomeOtherAlertJson))

      verify(mockAwsWrapper, never()).updateAlertNotification(any[Schedule])
      status(result) must equalTo(FORBIDDEN)
      contentAsString(result) must equalTo("")
    }

    "respond with 403 if user is neither team member/admin nor org admin" in running(FakeApplication(withGlobal = mockGlobal)) {

      Mockito.reset(mockAwsWrapper, mockDataManager, mockInfluxClientFactory)
      when(mockDataManager.findUserByToken(Matchers.eq(SOME_TOKEN), any[DateTime])
        (any[ExecutionContext])).thenReturn(Future.successful(Some(SOME_USER)))
      when(mockDataManager.getOrganization(GiltName)).thenReturn(Success(Some(GiltOrg)))
      when(mockDataManager.getTeam(GiltOrg, GiltTeamName)).thenReturn(Success(Some(GiltTeam)))
      when(mockDataManager.getTeamsForUser(GiltOrg, SOME_USER)).thenReturn(Future.successful(List(GiltTeamName -> Role.Viewer)))
      when(mockDataManager.getOrganizationsForUser(SOME_USER)).thenReturn(Future.successful(List(GiltName -> Role.Member)))

      val result = new TestController().updateTeamAlert(GiltName, GiltTeamName, AlertId)(
        FakeRequest(PUT, s"/organizations/$GiltName/teams/$GiltTeamName/alerts",
          FakeHeaders(Seq(AUTHORIZATION -> Seq(AUTH_TOKEN))),
          SomeOtherAlertJson))

      verify(mockAwsWrapper, never()).updateAlertNotification(any[Schedule])
      status(result) must equalTo(FORBIDDEN)
      contentAsString(result) must equalTo("")
    }

    "respond with 500 if an error occurs during get" in running(FakeApplication(withGlobal = mockGlobal)) {

      Mockito.reset(mockAwsWrapper, mockDataManager, mockInfluxClientFactory)
      when(mockDataManager.findUserByToken(Matchers.eq(SOME_TOKEN), any[DateTime])
        (any[ExecutionContext])).thenReturn(Future.successful(Some(SOME_USER)))
      when(mockDataManager.getOrganization(GiltName)).thenReturn(Success(Some(GiltOrg)))
      when(mockDataManager.getTeam(GiltOrg, GiltTeamName)).thenReturn(Success(Some(GiltTeam)))
      when(mockDataManager.getTeamsForUser(GiltOrg, SOME_USER)).thenReturn(Future.successful(List(GiltTeamName -> Role.Member)))
      when(mockAlertManager.getAlert(AlertId)).thenReturn(Failure(new RuntimeException(ErrorMessage)))

      val result = new TestController().updateTeamAlert(GiltName, GiltTeamName, AlertId)(
        FakeRequest(PUT, s"/organizations/$GiltName/teams/$GiltTeamName/alerts",
          FakeHeaders(Seq(AUTHORIZATION -> Seq(AUTH_TOKEN))),
          SomeOtherAlertJson))

      verify(mockAwsWrapper, never()).updateAlertNotification(any[Schedule])
      status(result) must equalTo(INTERNAL_SERVER_ERROR)
      contentAsString(result) must equalTo(InternalErrorMessage)
    }

    "respond with 500 if an error occurs during update" in running(FakeApplication(withGlobal = mockGlobal)) {

      Mockito.reset(mockAwsWrapper, mockDataManager, mockInfluxClientFactory)
      when(mockDataManager.findUserByToken(Matchers.eq(SOME_TOKEN), any[DateTime])
        (any[ExecutionContext])).thenReturn(Future.successful(Some(SOME_USER)))
      when(mockDataManager.getOrganization(GiltName)).thenReturn(Success(Some(GiltOrg)))
      when(mockDataManager.getTeam(GiltOrg, GiltTeamName)).thenReturn(Success(Some(GiltTeam)))
      when(mockDataManager.getTeamsForUser(GiltOrg, SOME_USER)).thenReturn(Future.successful(List(GiltTeamName -> Role.Member)))
      when(mockAlertManager.getAlert(AlertId)).thenReturn(Success(Some(SomeAlert)))
      when(mockAlertManager.updateAlert(any[Alert], any[AlertPatch])).thenReturn(Failure(new RuntimeException(ErrorMessage)))

      val result = new TestController().updateTeamAlert(GiltName, GiltTeamName, AlertId)(
        FakeRequest(PUT, s"/organizations/$GiltName/teams/$GiltTeamName/alerts",
          FakeHeaders(Seq(AUTHORIZATION -> Seq(AUTH_TOKEN))),
          SomeOtherAlertJson))

      verify(mockAwsWrapper, never()).updateAlertNotification(any[Schedule])
      status(result) must equalTo(INTERNAL_SERVER_ERROR)
      contentAsString(result) must equalTo(InternalErrorMessage)
    }
  }
}
