package be.fsoffe.imaging.migrator.model.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to identify the ViewOne label for a specific annotation field.
 * @author jbourlet
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AnnotationLabel {
	/**
	 * Define the label value.
	 * @return
	 */
	String value();
	/**
	 * Annotation types where this label shoud be print.
	 * @return
	 */
	String[] types() default { "TEXT", "STAMP", "HIGHLIGHT" };
}