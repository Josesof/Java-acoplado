package com.bolsadeideas.springboot.web.app.controller;


import java.io.IOException;
import java.net.MalformedURLException;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;



import javax.validation.Valid;


//import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

//import com.bolsadeideas.springboot.web.app.models.dao.IClienteDao;
import com.bolsadeideas.springboot.web.app.models.entity.Cliente;
import com.bolsadeideas.springboot.web.app.models.entity.Factura;
import com.bolsadeideas.springboot.web.app.models.service.IClienteService;
import com.bolsadeideas.springboot.web.app.models.service.IUploadFileService;
import com.bolsadeideas.springboot.web.app.models.service.UploadsFileServiceImpl;
import com.bolsadeideas.springboot.web.app.util.paginator.PageRender;

//@RequestMapping("/cliente")

@Controller
@SessionAttributes("cliente") // persiste el objeto hasta el final de la sesion para la creacion del id
public class ClienteController {

	// Inyectamos la interfaz a nuestro controlador
	@Autowired
	private IClienteService clienteService;

	@Autowired
	private IUploadFileService uploadFileService;
	
	@Autowired
	private MessageSource messageSource; 

	// Metodo que permite cargar la imagen
	@Secured("ROLE_ADMIN")
	@GetMapping(value = "/uploads/{filename:.+}")
	public ResponseEntity<Resource> verFoto(@PathVariable String filename) {

		Resource recurso = null;
		try {
			recurso = uploadFileService.load(filename);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + recurso.getFilename() + "\"")
				.body(recurso);
	}

	// Metodo que permite ver el detalle del cliente
	
	@GetMapping(value = "/ver/{id}")
	public String ver(@PathVariable(value = "id") Long id, Map<String, Object> model, RedirectAttributes flash) {
		Cliente cliente = clienteService.fetchByIdWithFacturas(id);
		if (cliente == null) {
			flash.addAttribute("error", "El cliente no existe en la base de datos");
			return "redirect:/listar";
		}
		model.put("cliente", cliente);
		model.put("titulo", "Detalle del cliente: " + cliente.getNombre());
		return "ver";
	}

	// Creamos un metodo que nos permite listar los clientes
	@RequestMapping(value = {"","/listar" }, method = RequestMethod.GET)
	public String listar(@RequestParam(name = "page", defaultValue = "0") int page, Model model,
			Locale locale) {
		Pageable pageRequest = PageRequest.of(page, 5);

		Page<Cliente> clientes = clienteService.findAll(pageRequest);
		PageRender<Cliente> pageRender = new PageRender<>("/listar", clientes);
		model.addAttribute("titulo", messageSource.getMessage("text.cliente.listar.titulo", null, locale));
		// En la interfaz se definio el metodo que permite listar todos los clientes
		model.addAttribute("clientes", clientes);
		model.addAttribute("page", pageRender);
		return "listar";
	}

	/***
	 * 
	 * // Creamos un metodo que nos permite listar los clientes
	 * 
	 * @RequestMapping(value = { "/listar", "", "/" }, method = RequestMethod.GET)
	 *                       public String listar(Model model) {
	 *                       model.addAttribute("titulo", "Listado de clientes"); //
	 *                       En la interfaz se definio el metodo que permite listar
	 *                       todos los clientes model.addAttribute("clientes",
	 *                       clienteService.findAll()); return "listar"; }
	 * 
	 */

	// Creamos un metodo que nos permite ver el formulario
	@Secured("ROLE_ADMIN")
	@RequestMapping(value = "/form", method = RequestMethod.GET)
	// Utlizamos en este caso Map que es lo mismo que model
	public String crear(Map<String, Object> model) {
		// Creamos una instancia de cliente
		Cliente cliente = new Cliente();
		model.put("cliente", cliente);
		model.put("titulo", "Formulario de cliente");
		return "form";
	}

	@RequestMapping(value = "/form/{id}")
	public String editar(@PathVariable(value = "id") Long id, Map<String, Object> model, RedirectAttributes flash) {

		Cliente cliente = null;
		if (id > 0) {
			cliente = clienteService.findOne(id);
			if (cliente == null) {
				flash.addFlashAttribute("error", "El cliente no existe en la base de datos");
			}
		} else {
			flash.addFlashAttribute("error", "El ID del cliente no puede ser Cero");
			return "redirect:listar";

		}
		model.put("cliente", cliente);
		model.put("titulo", "Editar cliente");
		return "form";
	}

	//// Metodo que nos permite guardar un cliente
	@Secured("ROLE_ADMIN")
	@RequestMapping(value = "/form", method = RequestMethod.POST)
	// Al metodo le pasamos como parametro el cliente desde el formulario
	// Como estamos utilizando validaciones marcamos con @Valid
	// Aunque es redundante el @ModelAttribute("clienteotro") lo marcamos porque en
	// algun otro caso
	// Vamos a tener un modelo con un nombre diferente entonces en este caso se debe
	// usar
	// Importamos BindingResult para la captura de errores
	// RedirectAttributes flash Para el manejo de alertas flash
	
	public String guardar(@Valid @ModelAttribute("cliente") Cliente cliente, BindingResult result, Model model,
			@RequestParam("file") MultipartFile foto, RedirectAttributes flash, SessionStatus status) {

		// Trabajando con los mensajes de error
		if (result.hasErrors()) {
			model.addAttribute("titulo", "Formulario de cliente");
			return "form";
		}

		if (!foto.isEmpty()) {

			if (cliente.getId() != null && cliente.getId() > 0 && cliente.getFoto() != null
					&& cliente.getFoto().length() > 0) {

				uploadFileService.delet(cliente.getFoto());
			}

			String uniqueFilename = null;
			try {
				uniqueFilename = uploadFileService.copy(foto);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Files.copy(foto.getInputStream(), rootAbsolutPath);
			flash.addFlashAttribute("info", "Has subido correctamente: " + uniqueFilename + "'");
			// Pasamos el nombre de la foto al cliente
			cliente.setFoto(uniqueFilename);
		}

		// Preguntamos si la foto no es vacia
		// if (!foto.isEmpty()) {
		// damos una ruta donde vamos a guardar nuestras fotos
		// Path directorioRecursos = Paths.get("src//main//resources//static//uploads");
		// asignamos a esta variable la ruta absoluta
		// String rooPath = directorioRecursos.toFile().getAbsolutePath();
		// vamos a capturar el error qu pueda surgir
		// Creando una ruta externa totalmente separada del proyecto
		// String rooPath = "C://Temp//uploads";

		// }

		String mensajeFlash = (cliente.getId() != null) ? "Cliente editado con exito" : "Cliente creado con exito!";
		// Guardamos con el metodo de la interfaz
		clienteService.save(cliente);
		// termina la sesion esto va conectado al id
		status.setComplete();
		flash.addFlashAttribute("success", mensajeFlash);
		// Redirecionamos a la vista listar
		return "redirect:listar";
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(value = "/eliminar/{id}")
	public String eliminar(@PathVariable(value = "id") Long id, RedirectAttributes flash) {
		if (id > 0) {
			Cliente cliente = clienteService.findOne(id);

			clienteService.delete(id);
			flash.addFlashAttribute("info", "Cliente eliminado con exito");

			if (uploadFileService.delet(cliente.getFoto())) {
				// flash.addFlashAttribute("info", "foto: " + cliente.getFoto() + " Eliminada
				// con exito");
			}

		}
		return "redirect:/listar";

	}
	
	
	private boolean hasRole(String role) {
		
		SecurityContext contex = SecurityContextHolder.getContext();
		
		if(contex == null) {
			return false;
		}
		
		Authentication auth = contex.getAuthentication(); 
		if(auth == null) {
			return false;
		}
		
		Collection <? extends GrantedAuthority> authorities = auth.getAuthorities();
		
		return authorities.contains(new SimpleGrantedAuthority(role));
		
		/**
		 * 
		 * 
		 * 	for(GrantedAuthority authority : authorities) {
			if(role.equals(authority.getAuthority())) {
				logger.info("Hola usuario" .concat(auth.getName()).concat("tu rol es: ").concat(authority.getAuthority()));
				return true;
			}
			
			return false;
		}
		 */
		
	
	}
	

}
