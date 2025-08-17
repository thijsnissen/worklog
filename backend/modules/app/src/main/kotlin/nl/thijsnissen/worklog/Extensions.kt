package nl.thijsnissen.worklog

import org.springframework.beans.factory.BeanRegistrar
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext

operator fun BeanRegistrar.plus(that: BeanRegistrar): BeanRegistrar =
    BeanRegistrar { registry, env ->
        this.register(registry, env)
        that.register(registry, env)
    }

operator fun BeanRegistrar.invoke(): ApplicationContextInitializer<GenericApplicationContext> =
    ApplicationContextInitializer { context ->
        context.register(this)
    }
