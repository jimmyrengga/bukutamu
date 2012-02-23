package bukutamu.controller;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRPdfExporter;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SessionStatus;

import bukutamu.model.Komentar;

@Controller
public class KomentarController {
	private List<Komentar> daftarKomentar = new ArrayList<Komentar>();

	@RequestMapping(value="/form", method=RequestMethod.GET)
	public ModelMap viewForm(){
		ModelMap mm = new ModelMap();
		mm.addAttribute(new Komentar());
		return mm ;
	}

	@RequestMapping(value="/form", method=RequestMethod.POST)
	public String prosesForm(@ModelAttribute @Valid Komentar k,
			BindingResult error,
			SessionStatus session){
		System.out.format("nama", k.getNama());
		System.out.format("email", k.getEmail());
		System.out.format("komentar", k.getKomentar());

		// cek error
		if(error.hasErrors()){
			return "form";
		}

		daftarKomentar.add(k);
		return "redirect:list" ;
	}

	@RequestMapping(value="/list", method=RequestMethod.GET)
	public ModelMap tampilkanForm(){
		ModelMap mm = new ModelMap();
		mm.addAttribute("daftarKomentar", daftarKomentar);
		return mm;
	}

	@RequestMapping(value="/report",method=RequestMethod.GET)
	public ModelMap reportKomentar(@RequestParam(required=false) String format){

		ModelMap mm = new ModelMap();

		if(StringUtils.hasText(format)){
			mm.addAttribute("format", format);
		} else {
			mm.addAttribute("format", "pdf");
		}

		mm.addAttribute("daftarKomentar", daftarKomentar);
		mm.addAttribute("judul", "Daftar Komentar");
		return mm;
	}

	@RequestMapping(value="/multipage",method=RequestMethod.GET)
	public void reportMultipage(HttpSession session, HttpServletResponse response) throws Exception {
		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "attachment; filename=contoh.pdf");

		// 1. compile report
		String page1template = "/WEB-INF/templates/jrxml/page1.jrxml";
		InputStream page1stream = session.getServletContext().getResourceAsStream(page1template);
		JasperReport page1report = JasperCompileManager.compileReport(page1stream);

		String page2template = "/WEB-INF/templates/jrxml/page2.jrxml";
		InputStream page2stream = session.getServletContext().getResourceAsStream(page2template);
		JasperReport page2report = JasperCompileManager.compileReport(page2stream);

		// 2. fill report
		Map<String, String> params = new HashMap<String, String>();
		params.put("isi", "Ini halaman pertama");
		JasperPrint page1print = JasperFillManager.fillReport(page1report, params);

		Map<String, String> params2 = new HashMap<String, String>();
		params2.put("pesan", "Ini halaman kedua");
		JasperPrint page2print = JasperFillManager.fillReport(page2report, params2);

		// 3. render
		List<JasperPrint> printList = new ArrayList<JasperPrint>();
		printList.add(page1print);
		printList.add(page2print);
		JRPdfExporter exporter=new JRPdfExporter();
		exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, printList);
		exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, response.getOutputStream());
		exporter.exportReport();

		// 4. cleanup
		response.getOutputStream().close();
	}

}
