package com.mhy.socialcommon;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Function：
 * Desc：带有这个注解的参数表示必传
 */
@Target(ElementType.PARAMETER)
public @interface ParamsRequired {
}
