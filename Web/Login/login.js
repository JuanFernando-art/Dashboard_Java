document.getElementById('formLogin').addEventListener('submit', async (event) => {
    event.preventDefault();

    const erro = document.getElementById('erroLogin');
    const botao = event.target.querySelector('button[type="submit"]');

    const credenciais = {
        email: document.getElementById('emailLogin').value.trim(),
        senha: document.getElementById('senhaLogin').value
    };

    erro.innerText = "";
    botao.disabled = true;
    botao.innerText = "Entrando...";

    try {
        const resposta = await fetch('http://localhost:7000/api/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(credenciais)
        });

        const contentType = resposta.headers.get('content-type') || '';
        const dados = contentType.includes('application/json')
            ? await resposta.json()
            : { erro: await resposta.text() };

        if (!resposta.ok) {
            throw new Error(dados.erro || "Nao foi possivel fazer login.");
        }

        localStorage.setItem('idUsuario', dados.idUsuario);
        localStorage.setItem('nomeUsuario', dados.nome);
        window.location.href = `/Homepage/homepage.html?idUsuario=${dados.idUsuario}`;
    } catch (error) {
        erro.innerText = error.message;
        botao.disabled = false;
        botao.innerText = "Entrar";
    }
});
