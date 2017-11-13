	var notepad;
	var caminho = 'ws://localhost:8080/ProjetoWeb3/notepad';
	
	window.onload = function() {
		
		document.getElementById("btnConnect").addEventListener("click", conectarSala, false);
		document.getElementById("btnCreateRoom").addEventListener("click", criarSala ,false);
		document.getElementById("arq").addEventListener("change", sendFile, false);
	}

	function criarSala() {
		var url = textoAleatorio(10);
		notepadEndpoint(url);
	}
	
	function conectarSala() {
		var urlId = document.getElementById("urlId").value;
		if(urlId === "" || urlId === null) {
			alert("Digite uma URL válida");
		} else {
			notepadEndpoint(document.getElementById("urlId").value);
		}
	}
	
	function textoAleatorio(tamanho) {
	    var letras = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
	    var aleatorio = '';
	    for (var i = 0; i < tamanho; i++) {
	        var rnum = Math.floor(Math.random() * letras.length);
	        aleatorio += letras.substring(rnum, rnum + 1);
	    }
	    return aleatorio;
	}
	
	function notepadEndpoint(urlId) {
		
		notepad = new WebSocket(caminho + "/" + urlId);
		
		notepad.onopen = onOpen;
		
		notepad.onmessage = onMessage;
		
		notepad.onclose = onClose;
		
		notepad.onerror = onError;	
	}
	

	function onError() {
		alert("Não foi possível comunicar-se com esta sala. Verifique se a URL esta correta ou se ela existe");
	}
	
	function onClose() {
		
		alert("Desconectou-se!");
		document.getElementById("content").value = "";
	}
	
	function onOpen() {
		alert("Conectado com sucesso");
		document.getElementById("content").focus();
		notepad.send(urlId.value);
	}
	
	function onMessage(e) {
		console.log(e.data);
		
		var sala = JSON.parse(e.data);
		
		if(sala.msgError !== 'NOT_OK') {
			if(sala.txt != undefined) {
				document.getElementById("infoSala").innerHTML = "A url da sua sala é: " + sala.id;
				document.getElementById("content").value = sala.txt;
			}
		} else {
			criarSala();
		}
	}
	
	function salvarTexto() {
		if(notepad != null) {
			notepad.send(document.getElementById("content").value);
		}
	}
	
	function FileSlicer(file) {
	    
	    this.sliceSize = 512*512;  
	    this.slices = Math.ceil(file.size / this.sliceSize);

	    this.currentSlice = 0;

	    this.getNextSlice = function() {
	        var start = this.currentSlice * this.sliceSize;
	        var end = Math.min((this.currentSlice+1) * this.sliceSize, file.size);
	        ++this.currentSlice;

	        return file.slice(start, end);
	    }
	}
	
	function sendFile() {
        var file = document.getElementById('arq').files[0];
        var fs = new FileSlicer(file);
        
        for(var i = 0; i < fs.slices; ++i) {
            notepad.send(fs.getNextSlice()); 
        }
    }