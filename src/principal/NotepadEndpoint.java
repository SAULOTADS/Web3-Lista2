package principal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import com.google.gson.Gson;

@ServerEndpoint(value = "/notepad/{url}")
public class NotepadEndpoint {

	private Session session;
	private static Set<SalaNotepad> salas = new CopyOnWriteArraySet<>();
	private static Map<String, List<NotepadEndpoint>> clientesPorSala = new HashMap<>();
	private static SalaNotepad sala = null;
	private static final String PATH_IMG = "WEB-INF" + File.separator + "imagens" + File.separator;
	
	@OnOpen
	public void onOpen(Session session, @PathParam("url") String url) {
		
		this.session = session;
		List<NotepadEndpoint> clientes = new ArrayList<>();
		
		if(verificarDisponibilidadeUrl(url)) {
			sala = new SalaNotepad();
			sala.setId(url);
			File arqImagens = new File(PATH_IMG + url);
			if(!arqImagens.exists()) {
				arqImagens.mkdirs();
			}
			sala.setPathImgs(PATH_IMG + url); 
			salas.add(sala);
		} else {
			sala = getSalaByUrl(url);
			clientes = clientesPorSala.get(url);
		}
		
		clientes.add(this);
		clientesPorSala.put(url, clientes);
	}
	
	@OnMessage
	public void onMessage(String message) {

		System.out.println(message);
		
		if(sala != null) {
			if(!message.equals(sala.getId())) {
				sala.setTxt(message);
			}	
		}
		
		enviarInfoSala(sala);
	}
	
	@OnError
	public void onError(Throwable t) {
		
	}
	
	@OnClose
	public void onClose() {
		clientesPorSala.get(sala.getId()).remove(this);
	}
	
	public static String toJson(Object o) {
		
		Gson gson = new Gson();
		return gson.toJson(o);
	}
	
	private boolean verificarDisponibilidadeUrl(String url) {
		
		boolean disponivel = true;
		boolean done = true;
		
		Iterator<SalaNotepad> salasNotepad = salas.iterator();
		
		while(salasNotepad.hasNext() && done) {
			if(salasNotepad.next().getId().equals(url)) {
				disponivel = false;
				done = false;
			}
		}
		
		return disponivel;
	}
	
	
	private static SalaNotepad getSalaByUrl(String url) {
		Map<String, SalaNotepad> salasPorUrl = salas.stream().collect(Collectors.toMap(SalaNotepad::getId, Function.identity()));
		return salasPorUrl.get(url);
	}
	
	private static void enviarInfoSala(SalaNotepad sala) {
		
		List<NotepadEndpoint> listClientes = clientesPorSala.get(sala.getId());
		listClientes.forEach(endpoint -> {
			
			synchronized (endpoint) {
				try {
					endpoint.session.getBasicRemote().sendText(toJson(sala));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}
}

