package nl.thijsnissen.worklog

import nl.thijsnissen.worklog.ports.incoming.WorklogServiceMock
import okhttp3.mockwebserver.MockWebServer
import org.springframework.beans.factory.BeanRegistrarDsl
import org.springframework.context.support.GenericApplicationContext
import org.springframework.test.web.reactive.server.WebTestClient

object WorklogServiceMockBean : BeanRegistrarDsl({ registerBean<WorklogServiceMock>() })

open class MockWebServerBean(mockWebServer: MockWebServer = MockWebServer()) :
    BeanRegistrarDsl({ registerBean<MockWebServer> { mockWebServer } })

open class WebTestClientBean(context: GenericApplicationContext) :
    BeanRegistrarDsl({
        registerBean<WebTestClient> { WebTestClient.bindToApplicationContext(context).build() }
    })
