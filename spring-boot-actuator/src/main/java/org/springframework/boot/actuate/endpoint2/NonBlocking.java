package org.springframework.boot.actuate.endpoint2;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicate that the {@link EndpointOperation} is non blocking and can be used in
 * a reactive context.
 *
 * @author Stephane Nicoll
 * @since 2.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NonBlocking {


}
