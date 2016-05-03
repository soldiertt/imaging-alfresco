package be.fsoffe.imaging.migrator;

import java.io.File;
import java.io.FilenameFilter;

/*
 * XLS "C:\Program Files\Microsoft Office\Office10\EXCEL.EXE"
CSV "C:\Program Files\Microsoft Office\Office10\EXCEL.EXE"
PPT "C:\Program Files\Microsoft Office\Office10\POWERPNT.EXE"
PPS "C:\Program Files\Microsoft Office\Office10\POWERPNT.EXE"
DOC "C:\Program Files\Microsoft Office\Office10\WINWORD.EXE"
RTF "C:\Program Files\Microsoft Office\Office10\WINWORD.EXE"
TXT "C:\WINNT\SYSTEM32\NOTEPAD.EXE"
PDF "\\dc1\apps\Adobe\Reader93\Adobe Reader 9.exe"
TIF "C:\xnview\xnview.exe"
GIF "C:\Program Files\Common Files\Microsoft Shared\PhotoEd\photoed.exe"
BMP "C:\Program Files\Common Files\Microsoft Shared\PhotoEd\photoed.exe"
JPG "C:\Program Files\Common Files\Microsoft Shared\PhotoEd\photoed.exe"
TIFF "C:\Program Files\Common Files\Microsoft Shared\PhotoEd\photoed.exe"
JPEG "C:\Program Files\Common Files\Microsoft Shared\PhotoEd\photoed.exe"
DOT "C:\Program Files\Microsoft Office\Office10\WINWORD.EXE"
VCF "C:\Program Files\Microsoft Office\Office10\WINWORD.EXE"
HTM "C:\Program Files\Internet Explorer\Iexplore.EXE"
HTML "C:\Program Files\Internet Explorer\Iexplore.EXE"
PNG "C:\xnview\xnview.exe"

 */
/**
 * FileName filter to authorize only specific extensions for imports file.
 *  
 * @author jbourlet
 *
 */
public class FFEImportsFileNameFilter implements FilenameFilter {

	@Override
	public boolean accept(File dir, String name) {
		if (name.endsWith(".xls") || name.endsWith(".csv") || name.endsWith(".ppt") || name.endsWith(".pps")
				|| name.endsWith(".doc") || name.endsWith(".rtf") || name.endsWith(".txt") || name.endsWith(".pdf")
				|| name.endsWith(".tif") || name.endsWith(".gif") || name.endsWith(".bmp") || name.endsWith(".jpg")
				|| name.endsWith(".tiff") || name.endsWith(".jpeg") || name.endsWith(".dot") || name.endsWith(".vcf")
				|| name.endsWith(".htm") || name.endsWith(".html") || name.endsWith(".png")) {
			return (null != dir && dir.exists());
        } else {
            return false;
        }
	}

}
