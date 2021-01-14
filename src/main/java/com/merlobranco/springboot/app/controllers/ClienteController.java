package com.merlobranco.springboot.app.controllers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.merlobranco.springboot.app.models.entity.Cliente;
import com.merlobranco.springboot.app.models.service.ClienteService;
import com.merlobranco.springboot.app.models.service.UploadFileService;
import com.merlobranco.springboot.app.util.paginator.PageRender;
import com.merlobranco.springboot.app.view.xml.ClienteList;

@Controller
@SessionAttributes("cliente")
public class ClienteController {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private static final int SIZE = 4;

	@Autowired
	private ClienteService clienteService;

	@Autowired
	private UploadFileService uploadFileService;
	
	@Autowired
	private MessageSource messageSource;

	@Secured("ROLE_USER")
	@GetMapping("/uploads/{filename:.+}")
	public ResponseEntity<Resource> verFoto(@PathVariable String filename) {
		Resource recurso = null;
		try {
			recurso = uploadFileService.load(filename);
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + recurso.getFilename() + "\"")
					.body(recurso);
		} catch (MalformedURLException e) {
			log.error("No ha podido cargar la imagen: " + filename, e);
		}
		return ResponseEntity.notFound().build();
	}

	@PreAuthorize("hasRole('ROLE_USER')")
	@GetMapping("/ver/{id}")
	public String ver(@PathVariable(value = "id") Long id, Model model, RedirectAttributes flash, Locale locale) {
		if (id <= 0) {
			flash.addFlashAttribute("error", "El ID del cliente no puede ser cero!");
			return "redirect:/listar";
		}

		Cliente cliente = clienteService.fetchByIdWithFacturas(id);
		if (cliente == null) {
			flash.addFlashAttribute("error", messageSource.getMessage("text.cliente.flash.db.error", null, locale));
			return "redirect:/listar";
		}
		model.addAttribute("titulo", messageSource.getMessage("text.cliente.detalle.titulo", null, locale).concat(": ").concat(cliente.getNombre()));
		model.addAttribute("cliente", cliente);
		return "ver";
	}
	
	@GetMapping("/listar-rest")
	public @ResponseBody ClienteList listarRest() {
		return new ClienteList(clienteService.findAll());
	}

	@GetMapping(value={"/listar", "/"})
	public String listar(@RequestParam(name = "page", defaultValue = "0") int page, Model model, Authentication authentication, HttpServletRequest request, Locale locale) {
		if (authentication !=null) {
			log.info("Hola Usuario autenticado, tu username es: ".concat(authentication.getName()));
		}
		
		// Getting the authentication object without injection, just through static access
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth !=null) {
			log.info("Utilizando acceso estático. Hola Usuario autenticado, tu username es: ".concat(auth.getName()));
		}
		
		if (hasRole("ROLE_ADMIN")) {
			log.info("Hola ".concat(auth.getName()).concat(" tienes acceso de administrador!"));
		}
		else {
			log.info("Hola ".concat(auth.getName()).concat(" NO tienes acceso de administrador!"));
		}
		
		SecurityContextHolderAwareRequestWrapper securityContext = new SecurityContextHolderAwareRequestWrapper(request, "");
		if (securityContext.isUserInRole("ROLE_ADMIN")) {
			log.info("Usando SecurityContextHolderAwareRequestWrapper. Hola ".concat(auth.getName()).concat(" tienes acceso de administrador!"));
		}
		else {
			log.info("Usando SecurityContextHolderAwareRequestWrapper. Hola ".concat(auth.getName()).concat(" NO tienes acceso de administrador!"));
		}
		
		if (request.isUserInRole("ROLE_ADMIN")) {
			log.info("Usando forma nativa HttpServletRequest. Hola ".concat(auth.getName()).concat(" tienes acceso de administrador!"));
		}
		else {
			log.info("Usando forma nativa HttpServletRequest. Hola ".concat(auth.getName()).concat(" NO tienes acceso de administrador!"));
		}
		
		Pageable pageRequest = PageRequest.of(page, SIZE);
		Page<Cliente> clientes = clienteService.findAll(pageRequest);
		PageRender<Cliente> pageRender = new PageRender<>("/listar", clientes);

		model.addAttribute("titulo", messageSource.getMessage("text.cliente.listar.titulo", null, locale));
		model.addAttribute("clientes", clientes);
		model.addAttribute("page", pageRender);
		return "listar";
	}

	@Secured("ROLE_ADMIN")
	@GetMapping("/form")
	public String crear(Model model, Locale locale) {
		Cliente cliente = new Cliente();
		model.addAttribute("titulo", messageSource.getMessage("text.cliente.form.titulo.crear", null, locale));
		model.addAttribute("cliente", cliente);
		return "form";
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@GetMapping("/form/{id}")
	public String editar(@PathVariable(value = "id") Long id, Model model, RedirectAttributes flash, Locale locale) {
		if (id <= 0) {
			flash.addFlashAttribute("error", messageSource.getMessage("text.cliente.flash.id.error", null, locale));
			return "redirect:/listar";
		}

		Cliente cliente = clienteService.findOne(id);
		if (cliente == null) {
			flash.addFlashAttribute("error", messageSource.getMessage("text.cliente.flash.db.error", null, locale));
			return "redirect:/listar";
		}
		model.addAttribute("titulo", messageSource.getMessage("text.cliente.form.titulo.editar", null, locale));
		model.addAttribute("cliente", cliente);
		return "form";
	}

	@Secured("ROLE_ADMIN")
	@PostMapping("/form")
	public String guardar(@Valid Cliente cliente, BindingResult result, Model model,
			@RequestParam("file") MultipartFile foto, RedirectAttributes flash, SessionStatus status, Locale locale) {
		if (result.hasErrors()) {
			model.addAttribute("titulo", messageSource.getMessage("text.cliente.form.titulo", null, locale));
			return "form";
		}

		if (!foto.isEmpty()) {
			if (cliente.getId() != null && cliente.getId() > 0 && cliente.getFoto() != null
					&& cliente.getFoto().length() > 0) {
				uploadFileService.delete(cliente.getFoto());
			}
			String uniqueFilename = "";
			try {
				uniqueFilename = uploadFileService.copy(foto);
				flash.addFlashAttribute("info", messageSource.getMessage("text.cliente.flash.foto.subir.success", null, locale) + "'" + uniqueFilename + "'");
				cliente.setFoto(uniqueFilename);
			} catch (IOException e) {
				flash.addFlashAttribute("error", messageSource.getMessage("text.cliente.flash.foto.subir.error", null, locale));
			}
		}
		String mensajeFlash = (cliente.getId() != null) ? messageSource.getMessage("text.cliente.flash.editar.success", null, locale) : messageSource.getMessage("text.cliente.flash.crear.success", null, locale);

		clienteService.save(cliente);
		status.setComplete();
		flash.addFlashAttribute("success", mensajeFlash);
		return "redirect:/listar";
	}

	@Secured("ROLE_ADMIN")
	@GetMapping("/eliminar/{id}")
	public String eliminar(@PathVariable(value = "id") Long id, RedirectAttributes flash, Locale locale) {
		if (id > 0) {
			Cliente cliente = clienteService.findOne(id);

			clienteService.delete(id);
			flash.addFlashAttribute("success", messageSource.getMessage("text.cliente.flash.eliminar.success", null, locale));

			if (uploadFileService.delete(cliente.getFoto())) {
				String mensajeFotoEliminar = String.format(messageSource.getMessage("text.cliente.flash.foto.eliminar.success", null, locale), cliente.getFoto());
				flash.addFlashAttribute("info", mensajeFotoEliminar);
			}
		}
		return "redirect:/listar";
	}
	
	private boolean hasRole(String role) {
		if (role == null || role.isEmpty())
			return false;
		
		SecurityContext context = SecurityContextHolder.getContext();
		if (context == null)
			return false;
		
		Authentication auth = context.getAuthentication();
		if (auth == null)
			return false;
		
		//return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role));
	
		// Other option
		return auth.getAuthorities().contains(new SimpleGrantedAuthority(role));
	} 

}
