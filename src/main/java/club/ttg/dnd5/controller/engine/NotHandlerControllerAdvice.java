package club.ttg.dnd5.controller.engine;

import club.ttg.dnd5.exception.PageExistException;
import club.ttg.dnd5.exception.PageNotFoundException;
import club.ttg.dnd5.exception.StorageException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Date;

@ControllerAdvice
public class NotHandlerControllerAdvice {
	@Value("${frontend.application.sha:1}")
	private String version;
	@Value("${spring.profiles.active:prod}")
	private String profile;

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(PageNotFoundException.class)
	public String handlePageNotFound(Exception exception, Model model, HttpServletRequest request) {
		addAttributes(request, model);
		return "spa";
	}

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(StorageException.class)
	public String handleStorageException(Exception exception, Model model, HttpServletRequest request) {
		addAttributes(request, model);
		return "spa";
	}

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(NoHandlerFoundException.class)
	public String handleNoHandlePageException(Exception exception, Model model, HttpServletRequest request) {
		addAttributes(request, model);
		return "spa";
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(PageExistException.class)
	public String handleHandlePageExistException(Exception exception, Model model, HttpServletRequest request) {
		addAttributes(request, model);
		return "spa";
	}

	private void addAttributes(HttpServletRequest request, Model model) {
		if (version == null || version.isEmpty()) {
			version = String.valueOf(new Date().getTime());
		}

		model.addAttribute("version", version);
		model.addAttribute("profile", profile);

		String themeName = "dark";
		Cookie[] cookies = request.getCookies();

		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("ttg_theme_name")) {
					themeName = cookie.getValue();
				}
			}
		}
		model.addAttribute("themeName", themeName);
	}
}
