package unet.kad4.rpc.events.inter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {

    String value();

    unet.kad4.rpc.events.inter.Priority priority() default Priority.DEFAULT;
}
