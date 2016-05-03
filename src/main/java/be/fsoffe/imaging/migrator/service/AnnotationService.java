package be.fsoffe.imaging.migrator.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import be.fsoffe.imaging.migrator.model.annotation.ImagingAnnotation;
/**
 * Service to manage the annotation migration procedure.
 * 
 * @author jbourlet
 *
 */
public class AnnotationService {
	
	private static final float CONVERSION_RATIO = 2.4f;
	
	/**
	 * Generate the content of the annotation text file based on exported uewi annotation properties.
	 * @param annotations uewi annotation fields
	 * @return annotation file content
	 */
	public String generateAnnotationFileText(JSONObject annotations) {
		
		List<ImagingAnnotation> imgAnnotations = parseAnnotations(annotations);
		if (imgAnnotations.size() > 0) {
			StringBuilder annotationBuilder = new StringBuilder("");
			for (ImagingAnnotation imagingAnnotation : imgAnnotations) {
				annotationBuilder.append(imagingAnnotation.toString()).append("\n");
			}
			return annotationBuilder.toString();
		} else {
			return null;
		}
		
	}
	
	/**
	 * Convert uewi annotations to ViewOne annotations.
	 * @param annotations uewi annotations
	 * @return list of ImagingAnnotation
	 */
	private List<ImagingAnnotation> parseAnnotations(JSONObject annotations) {
		List<ImagingAnnotation> imagingAnnotations = new ArrayList<ImagingAnnotation>();
		@SuppressWarnings("unchecked")
		Iterator<Map.Entry<String, JSONArray>> it = annotations.entrySet().iterator();
	    while (it.hasNext()) {
			Map.Entry<String, JSONArray> entry = it.next();
	    	//LOOP OVER PAGES
            String page = entry.getKey();
			for (int i = 0; i < (entry.getValue()).size(); i++) {
				//LOOP OVER ANNOTATION IN A PAGE
				JSONObject uewiAnnotation = (JSONObject) ((JSONArray) entry.getValue()).get(i);
				imagingAnnotations.add(mapUewiToImagingAnnotation(uewiAnnotation, page));
			}
	    }
		return imagingAnnotations;
	}
	
	/**
	 * Convert uewi annotations to ViewOne annotations for a single page.
	 * 
	 * @param uewiAnnotation uewiAnnotation for a single page
	 * @param page the page name
	 * @return an ImagingAnnotation object
	 */
	private ImagingAnnotation mapUewiToImagingAnnotation(JSONObject uewiAnnotation, String page) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm:ss");
		String author = (String) uewiAnnotation.get("Author");
		ImagingAnnotation imgAnnot = new ImagingAnnotation(author);
		int annotType = ((Long) uewiAnnotation.get("Type")).intValue();
		int annotSubType = ((Long) uewiAnnotation.get("SubType")).intValue();
		if (annotType == 3 || annotType == 0) {
			imgAnnot.setType("TEXT");
			imgAnnot.setTransparent(0);
			imgAnnot.setLabel("Text");
			imgAnnot.setBorder((long) ((Long) uewiAnnotation.get("Border")));
		} else if (annotType == 1) {
			imgAnnot.setType("HIGHLIGHT");
			imgAnnot.setTransparent(1);
			imgAnnot.setLabel("Highlight");
		} else if (annotType == 2) {
			imgAnnot.setType("STAMP");
			imgAnnot.setResource(mapUewiToImagingStamp(annotSubType));
			imgAnnot.setLabel("Stamp");
		}
		imgAnnot.setCreator(author);
		imgAnnot.setModifier(author);
		imgAnnot.setPage(Long.parseLong(page.substring(4)));
		
		imgAnnot.setPosX((long) ((Long) uewiAnnotation.get("Left") * CONVERSION_RATIO));
		imgAnnot.setPosY((long) ((Long) uewiAnnotation.get("Top") * CONVERSION_RATIO));
		imgAnnot.setHeight((long) ((Long) uewiAnnotation.get("Height") * CONVERSION_RATIO));
		imgAnnot.setWidth((long) ((Long) uewiAnnotation.get("Width") * CONVERSION_RATIO));
		imgAnnot.setBackgroundColor(mapUewiToImagingColor(((Long) uewiAnnotation.get("BackgroundColor")).intValue()));
		imgAnnot.setSemiTransparent((Long) uewiAnnotation.get("Transparent"));
		imgAnnot.setCreatedDate(sdf.format(new Date((Long) uewiAnnotation.get("TimeCreated") * 1000)).concat(", CEST"));
		imgAnnot.setModifiedDate(sdf.format(new Date((Long) uewiAnnotation.get("TimeModified") * 1000)).concat(", CEST"));
		imgAnnot.getAnnotationText().setFontFamily((String) uewiAnnotation.get("FontName"));
		imgAnnot.getAnnotationText().setFontHeight((Long) uewiAnnotation.get("FontSize") * 3);
		if (uewiAnnotation.get("Text") != null) {
			//Fix line breaks issue with too many line breaks
			String fixedLineBreaks = ((String) uewiAnnotation.get("Text")).replaceAll("~~", "~");
			imgAnnot.getAnnotationText().setText(fixedLineBreaks.replaceAll("~", "<N>"));
		}
		imgAnnot.getAnnotationText().setColor(mapUewiToImagingColor(((Long) uewiAnnotation.get("TextColor")).intValue()));
		return imgAnnot;
	}
	
	/**
	 * Map uewi colors to new color value.
	 * @param color uewi color code
	 * @return rgb color as a string
	 */
	private String mapUewiToImagingColor(int color) {
		switch (color) {
			case 0: return "0, 0, 0"; 		// BLACK
			case 1: return "255, 0, 0"; 	// LIGHT RED
			case 2: return "0, 255, 0"; 	// LIGHT GREEN
			case 3: return "0, 0, 255"; 	// LIGHT BLUE
			case 4: return "255, 255, 0"; 	// LIGHT YELLOW
			case 5: return "0, 255, 255"; 	// LIGHT CYAN
			case 6: return "255, 0, 255"; 	// LIGHT MAGENTA
			case 7: return "255, 255, 255"; // WHITE
			case 8: return "128, 128, 128"; // DARK GRAY
			case 9: return "192, 192, 192"; // LIGHT GRAY
			case 10: return "192, 0, 192"; 	// LIGHT PURPLE
			case 11: return "0, 0, 128"; 	// DARK BLUE
			case 12: return "0, 128, 0"; 	// DARK GREEN
			case 13: return "128, 0, 0"; 	// DARK RED
			case 14: return "0, 128, 128"; 	// DARK CYAN
			case 15: return "128, 0, 128";	// DARK MAGENTA
			default: return "0, 0, 0";
		}
	}
	
	/**
	 * Map uewi stamp to new stamp path.
	 * @param subType the uewi annotation stamp code
	 * @return the new resource path
	 */
	private String mapUewiToImagingStamp(int subType) {
		switch (subType) {
			case 1: return "image:/share/res/imaging/img/annot/approved.png<255,255,255>";
			case 2: return "image:/share/res/imaging/img/annot/rejected.png<129,129,129>";
			case 3: return "image:/share/res/imaging/img/annot/overdue.png<255,255,255>"; 
			case 4: return "image:/share/res/imaging/img/annot/check.png<255,255,255>"; 
			case 5: return "image:/share/res/imaging/img/annot/cross.png<255,255,255>"; 
			case 6: return "image:/share/res/imaging/img/annot/question.png<255,255,255>";
			case 7: return "image:/share/res/imaging/img/annot/exclamation.gif<206,237,220>";
			case 8: return "image:/share/res/imaging/img/annot/star.png<255,255,255>"; 
			case 9: return "image:/share/res/imaging/img/annot/phone.png<0,0,0>";
			case 10: return "image:/share/res/imaging/img/annot/circle.png<255,255,255>";
			case 11: return "image:/share/res/imaging/img/annot/downarrow.png<255,255,255>"; 
			case 12: return "image:/share/res/imaging/img/annot/uparrow.png<255,255,255>"; 
			default: return "";
		}
	}
}
