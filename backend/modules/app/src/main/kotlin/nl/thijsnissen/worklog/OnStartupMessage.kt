package nl.thijsnissen.worklog

import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent

class OnStartupMessage : ApplicationListener<ContextRefreshedEvent> {
    override fun onApplicationEvent(event: ContextRefreshedEvent) =
        log.info(
            """


            Worklog - Simple service for logging Toggl Track entries in Jira Tempo

            Interact with the application in one of the following ways:
            - Through the GUI exposed at http://localhost:8080
            - Through the API documented at http://localhost:8080/api/v1/docs
            - Through Swagger UI at http://localhost:8080/api/v1/swagger-ui

            """
                .trimIndent()
        )

    companion object {
        private val log = LoggerFactory.getLogger(OnStartupMessage::class.java)
    }
}
