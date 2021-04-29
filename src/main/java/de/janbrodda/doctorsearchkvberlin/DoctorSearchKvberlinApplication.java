package de.janbrodda.doctorsearchkvberlin;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import lombok.ToString;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class DoctorSearchKvberlinApplication {

	public static void main(String[] args) throws IOException {
		OkHttpClient client = new OkHttpClient();

		StringBuilder sb = new StringBuilder();
		sb.append("firstname;");
		sb.append("lastname;");
		sb.append("email;");
		sb.append("phone;");
		sb.append("address");
		sb.append('\n');

		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet();
		Row headerRow = sheet.createRow(sheet.getLastRowNum() + 1);
		headerRow.createCell(0).setCellValue("First Name");
		headerRow.createCell(headerRow.getLastCellNum()).setCellValue("Last Name");
		headerRow.createCell(headerRow.getLastCellNum()).setCellValue("Email");
		headerRow.createCell(headerRow.getLastCellNum()).setCellValue("Phone");
		headerRow.createCell(headerRow.getLastCellNum()).setCellValue("Address");

		for (char c = 'A'; c <= 'Z'; ++c) {
			String url = "https://www.kvberlin.de/typo3conf/ext/arztsuche/Classes/Controller/connector_ind.php"
					+ "?url=52.520008%2F13.404954%2F40.json%3Fn%3D" + c + "%26fg%3D470";
			Request request = new Request.Builder().url(url).build();
			System.out.println(url);

			ObjectMapper objectMapper = new ObjectMapper()
					.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			ResponseBody responseBody = client.newCall(request).execute().body();
			List<Doctor> doctors = objectMapper.readValue(responseBody.string(),
														  objectMapper.getTypeFactory().constructCollectionType(List.class, Doctor.class));

			System.out.println(doctors);


			doctors.stream().filter(d -> !d.e.isEmpty() || !d.te.isEmpty()).forEach(d -> {
				Row row = sheet.createRow(sheet.getLastRowNum() + 1);
				row.createCell(0).setCellValue(d.v);
				row.createCell(row.getLastCellNum()).setCellValue(d.n);
				row.createCell(row.getLastCellNum()).setCellValue(d.e);
				row.createCell(row.getLastCellNum()).setCellValue(d.te);
				row.createCell(row.getLastCellNum()).setCellValue(d.getAddress());

				sb.append(d.v + ";");
				sb.append(d.n + ";");
				sb.append(d.e + ";");
				sb.append(d.te + ";");
				sb.append(d.getAddress() + "\n");
			});
		}

		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream("doctors-berlin.csv"), StandardCharsets.UTF_8));
		writer.write(sb.toString());
		writer.close();

		FileOutputStream outputStream = new FileOutputStream("doctors-berlin.xlsx");
		workbook.write(outputStream);
		workbook.close();
	}

	@Setter
	@ToString
	private static class Doctor {
		// email
		private String e;

		// last name
		private String n;

		// first name
		private String v;

		// phone
		private String te;

		// street
		private String st;

		// house number
		private String ha;

		// zip code
		private String pl;

		// city
		private String o;

		public String getAddress() {
			return st + " " + ha + ", " + pl + " " + o;
		}

	}

}
