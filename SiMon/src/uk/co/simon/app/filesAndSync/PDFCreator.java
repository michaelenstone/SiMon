package uk.co.simon.app.filesAndSync;

import harmony.java.awt.Color;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import uk.co.simon.app.R;
import uk.co.simon.app.sqllite.DataSourceLocations;
import uk.co.simon.app.sqllite.DataSourcePhotos;
import uk.co.simon.app.sqllite.DataSourceProjects;
import uk.co.simon.app.sqllite.DataSourceReportItems;
import uk.co.simon.app.sqllite.DataSourceReports;
import uk.co.simon.app.sqllite.SQLLocation;
import uk.co.simon.app.sqllite.SQLPhoto;
import uk.co.simon.app.sqllite.SQLProject;
import uk.co.simon.app.sqllite.SQLReport;
import uk.co.simon.app.sqllite.SQLReportItem;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.Toast;

import com.lowagie.text.Annotation;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class PDFCreator extends AsyncTask<Void, Void, String> {
	
	SQLReport thisReport;
	Context context;
	ProgressDialog progress;
	String path;

	public PDFCreator(SQLReport report, Context context, ProgressDialog progress) {
		this.thisReport = report;
		this.context = context;
		this.progress = progress;
	}

	@Override
	protected String doInBackground(Void... params) {
		path = null;
		try {
			path = createPDF();
		} catch (DocumentException e) {
			return "Document Exception: " + e.toString();
		} catch (IOException e) {
			return "I/O Exception: " + e.toString();
		}
		return null;
	}
	
	protected void onPostExecute(String result) {
		String message = new String();
		if (result==null){			
			thisReport.setPDF(path);
			DataSourceReports datasource = new DataSourceReports(context);
			datasource.open();
			datasource.updateReport(thisReport);
			datasource.close();
			message = "PDF Created";
		} else {
			message = "Could not create PDF - Error: " + result;
		}
		Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
		try {
			progress.dismiss();
		} catch (Exception e) {
		}
		toast.show();
	}

	
	public String createPDF() throws DocumentException, IOException {

		DataSourceProjects datasource = new DataSourceProjects(context);
		DataSourceReportItems datasourceReportItems = new DataSourceReportItems(context);
		DataSourcePhotos datasourcePhotos = new DataSourcePhotos(context);
		DataSourceLocations datasourceLocations = new DataSourceLocations(context);
		
		Font titleFont = new Font();
		titleFont.setStyle(Font.TIMES_ROMAN);
		titleFont.setSize(25);
		
		Document doc = new Document();
		String pdfPath = getOutputFile(thisReport, context);
		OutputStream pdfFile = new FileOutputStream(pdfPath);
		PdfWriter.getInstance(doc, pdfFile);
		doc.open();

		PdfPTable title = new PdfPTable(1);
		
		PdfPCell titleCell = new PdfPCell();
		Paragraph titlePara = new Paragraph();
		if (thisReport.getReportType()) {
			titlePara.add(context.getResources().getString(R.string.pdfSiteVisitReportTitle));
		} else {
			titlePara.add(context.getResources().getString(R.string.pdfProgressReportTitle));
		}
		titlePara.setSpacingAfter(5);
		titlePara.setFont(titleFont);
		titleCell.addElement(titlePara);
		titleCell.setBorder(Rectangle.BOTTOM);
		title.addCell(titleCell);
		title.setSpacingAfter(25);
		doc.add(title);
		
		PdfPTable header = new PdfPTable(4);

        header.addCell(new PdfPCell(new Paragraph(context.getResources().getString(R.string.dailyProgressProject))));
        datasource.open();
        SQLProject project = datasource.getProject(thisReport.getProjectId());
        header.addCell(new PdfPCell(new Paragraph(project.toString())));
        datasource.close();
        header.addCell(new PdfPCell(new Paragraph(context.getResources().getString(R.string.dailyProgressDate))));
        header.addCell(new PdfPCell(new Paragraph(thisReport.getReportDate())));
        
        header.addCell(new PdfPCell(new Paragraph(context.getResources().getString(R.string.dailyProgressSupervisor))));
        header.addCell(new PdfPCell(new Paragraph(thisReport.getSupervisor())));
        header.addCell(new PdfPCell(new Paragraph(context.getResources().getString(R.string.dailyProgressReportRef))));
        header.addCell(new PdfPCell(new Paragraph(thisReport.getReportRef())));

        header.addCell(new PdfPCell(new Paragraph(context.getResources().getString(R.string.dailyProgressWeather))));
        header.addCell(new PdfPCell(new Paragraph(thisReport.getWeather())));
        header.addCell(new PdfPCell(new Paragraph(context.getResources().getString(R.string.dailyProgressTemp))));
        String[] tempType = context.getResources().getStringArray(R.array.dailyProgressTempType);
        header.addCell(new PdfPCell(new Paragraph(thisReport.getTemp() + " " + tempType[(int) thisReport.getTempType()])));
        
        header.setSpacingAfter(25);
        
        doc.add(header);

		PdfPTable contents = new PdfPTable(2);
		
		float[] columnWidths = {10f, 90f};
		contents.setWidths(columnWidths);
		
		contents.addCell(new PdfPCell(new Paragraph(context.getResources().getString(R.string.pdfNoColumn))));
		contents.addCell(new PdfPCell(new Paragraph(context.getResources().getString(R.string.pdfItemColumn))));
        
		datasourceReportItems.open();
		datasourceLocations.open();			
		datasourcePhotos.open();
		
		java.util.List<SQLReportItem> reportItems = datasourceReportItems.getReportItems(thisReport.getId());
		
		int i = 1;
		
		for (SQLReportItem reportItem : reportItems) {
			//Row 1
			PdfPCell row1Cell1 = new PdfPCell();
			row1Cell1.addElement(new Paragraph(Integer.toString(i)));
			row1Cell1.setBorder(Rectangle.RIGHT);
			contents.addCell(row1Cell1);
			
			PdfPCell row1Cell2 = new PdfPCell();
			SQLLocation location = datasourceLocations.getLocation(reportItem.getLocationId());
			row1Cell2.addElement(new Paragraph(
					context.getResources().getString(R.string.dailyProgressLocation) +
					": " + 
					location.getLocation()));
			row1Cell2.setBorder(Rectangle.LEFT);
			contents.addCell(row1Cell2);
			
			//Row 2
			PdfPCell row2Cell1 = new PdfPCell();
			row2Cell1.addElement(new Paragraph(" "));
			row2Cell1.setBorder(Rectangle.RIGHT);
			contents.addCell(row2Cell1);
			
			PdfPCell row2Cell2 = new PdfPCell();
			row2Cell2.addElement(new Paragraph(
					context.getResources().getString(R.string.dailyProgressActivity) +
					": " + 
					reportItem.getReportItem()));
			row2Cell2.setBorder(Rectangle.LEFT);
			contents.addCell(row2Cell2);
			
			if (!thisReport.getReportType()) {
				//Row 3
				PdfPCell row3Cell1 = new PdfPCell();
				row3Cell1.addElement(new Paragraph(" "));
				row3Cell1.setBorder(Rectangle.RIGHT);
				contents.addCell(row3Cell1);

				PdfPCell row3Cell2 = new PdfPCell();
				String[] onTimeArray = context.getResources().getStringArray(R.array.dailyProgressOnTimeSpinner);
				Chunk progress = new Chunk(context.getResources().getString(R.string.pdfProgress) +	": " + 
						reportItem.getProgress() + "% - ");
				Chunk onTime = new Chunk(reportItem.getOnTIme());
				if (reportItem.getOnTIme().contains(onTimeArray[0])) {
					onTime.setBackground(Color.GREEN);
				} else if (reportItem.getOnTIme().contains(onTimeArray[1])) {
					onTime.setBackground(Color.ORANGE);
				} else if (reportItem.getOnTIme().contains(onTimeArray[2])) {
					onTime.setBackground(Color.RED);
				}

				Paragraph progressPara = new Paragraph();
				progressPara.add(progress);
				progressPara.add(onTime);
				row3Cell2.addElement(progressPara);
				row3Cell2.setBorder(Rectangle.LEFT);
				contents.addCell(row3Cell2);
			}
			
			//Row 4
			PdfPCell row4Cell1 = new PdfPCell();
			row4Cell1.addElement(new Paragraph(" "));
			row4Cell1.setBorder(Rectangle.RIGHT);
			contents.addCell(row4Cell1);
			
			PdfPCell row4Cell2 = new PdfPCell();
			row4Cell2.addElement(new Paragraph(
					context.getResources().getString(R.string.dailyProgressDescription) +
					": " + 
					reportItem.getDescription()));
			row4Cell2.setBorder(Rectangle.LEFT);
			contents.addCell(row4Cell2);

			java.util.List<SQLPhoto> photos = datasourcePhotos.getReportItemPhotos(reportItem.getId());
			PdfPCell rowPCell1 = new PdfPCell();
			rowPCell1.addElement(new Paragraph(" "));
			rowPCell1.setBorder(Rectangle.RIGHT);
			contents.addCell(rowPCell1);
			PdfPCell rowPCell2 = new PdfPCell();
			
			PdfPTable photoTable = new PdfPTable(2);
			int x = 0;
			for (SQLPhoto photo : photos) {
				
				PdfPCell photoCell = new PdfPCell();
		    	final BitmapFactory.Options options = new BitmapFactory.Options();
		    	options.inJustDecodeBounds = false;
		    	double scale = 1.4;
		    	Bitmap rawImage = BitmapFactory.decodeFile(photo.getPhotoPath(), options);
		    	Bitmap scaledBitmap = Bitmap.createScaledBitmap(rawImage, (int)Math.ceil(options.outWidth/scale), (int)Math.ceil(options.outHeight/scale), true);
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream);
				Image image = Image.getInstance(stream.toByteArray());
				image.setCompressionLevel(9);
				image.scaleToFit(250, 250);
				String orientation = Float.toString(photo.getAzimuth()) + ":" + Float.toString(photo.getPitch()) + ":" + Float.toString(photo.getRoll());
				String gpsPosition = Float.toString(photo.getGPSX()) + ":" + Float.toString(photo.getGPSY()) + ":" + Float.toString(photo.getGPSZ());
				Annotation annotation = new Annotation(orientation, gpsPosition);
				image.setAnnotation(annotation);
				photoCell.addElement(new Chunk(image,0,0));
				photoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
				photoCell.setBorder(Rectangle.NO_BORDER);
				photoTable.addCell(photoCell);
				x++;
				rawImage.recycle();
				scaledBitmap.recycle();
			}
			if (x % 2 != 0) {
				PdfPCell blankCell = new PdfPCell();
				blankCell.addElement(new Paragraph(" "));
				blankCell.setBorder(Rectangle.NO_BORDER);
				photoTable.addCell(blankCell);
			}
			rowPCell2.addElement(photoTable);
			rowPCell2.setBorder(Rectangle.LEFT);
			contents.addCell(rowPCell2);
			i++;
		}
		datasourcePhotos.close();
		datasourceLocations.close();
		datasourceReportItems.close();
		doc.add(contents);
		doc.close();
		return pdfPath;
	}
	
	private static String getOutputFile(SQLReport thisReport, Context context) throws IOException {

		File StorageDir = FileManager.getPDFStorageLocation(context);

		if (StorageDir!=null) {
			String timeStamp = new SimpleDateFormat("yyyy-MM-dd_").format(new Date());
			char fileSep = '/'; // ... or do this portably.
			char escape = '-'; // ... or some other legal char.
			int len = thisReport.getReportRef().length();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < len; i++) {
			    char ch = thisReport.getReportRef().charAt(i);
			    if (ch < ' ' || ch >= 0x7F || ch == fileSep || (ch == '.') || ch == escape) {
			        sb.append(escape);
			    } else {
			        sb.append(ch);
			    }
			}
			String pdfFile = StorageDir.getPath() + File.separator + timeStamp + sb.toString() + ".pdf";
			return pdfFile;
		} else {
			return null;
		}
	}
}
