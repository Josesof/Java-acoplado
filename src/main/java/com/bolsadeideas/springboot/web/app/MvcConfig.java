package com.bolsadeideas.springboot.web.app;

import java.nio.file.Paths;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;


//Creamos una nueva clase configure que implemente la interfaz WebMvcConfigurer para el manejo de rutas y mapeas
//Con directorios externos
@Configuration
public class MvcConfig implements WebMvcConfigurer {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	//Comentamos el codigo por que vamos a cargar las imagenes de una manera distinta pero este codigo es funcional.
	/*
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// TODO Auto-generated method stub
		WebMvcConfigurer.super.addResourceHandlers(registry);
		//Vamos a modificar la ruta para que sea absoluta.
		String resourcePath = Paths.get("uploads").toAbsolutePath().toUri().toString();
		//mapeo de la url ruta y ubicacion de la carpeta en el local  los ** hacen referencia al nombre de la foto.
		//registry.addResourceHandler("/uploads/**").addResourceLocations("file:/C:/Temp/uploads/");
		registry.addResourceHandler("/uploads/**").addResourceLocations(resourcePath);
		log.info("resourcePath:" + resourcePath);
	}

	*/
	
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/error_403").setViewName("error_403");
	}
	
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public LocaleResolver localeResolver() {
		SessionLocaleResolver localeResolver = new SessionLocaleResolver();
		localeResolver.setDefaultLocale(new Locale("es","ES"));
		return localeResolver;
	}
	
	@Bean
	public LocaleChangeInterceptor localeChangeInterceptor() {
		LocaleChangeInterceptor localeInterceptor = new LocaleChangeInterceptor();
		localeInterceptor.setParamName("lang");
		return localeInterceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// TODO Auto-generated method stub
		registry.addInterceptor(localeChangeInterceptor());
	}
	
	
	@Bean
	public Jaxb2Marshaller jaxb2Marshaller() {
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		marshaller.setClassesToBeBound(new Class []{com.bolsadeideas.springboot.web.app.view.xml.ClienteList.class});
		return  marshaller;
	}
	
}
