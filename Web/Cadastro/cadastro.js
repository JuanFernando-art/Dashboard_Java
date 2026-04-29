document.getElementById('formCadastro').addEventListener('submit', async (event) => {
    event.preventDefault();

    const erro = document.getElementById('erroCadastro');
    const botao = event.target.querySelector('button[type="submit"]');

    const usuario = {
        nome: document.getElementById('nomeCadastro').value.trim(),
        cpf: document.getElementById('cpfCadastro').value.trim(),
        email: document.getElementById('emailCadastro').value.trim(),
        senha: document.getElementById('senhaCadastro').value
    };

    erro.innerText = "";
    botao.disabled = true;
    botao.innerText = "Cadastrando...";

    try {
        const resposta = await fetch('http://localhost:7000/api/usuarios', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(usuario)
        });

        const dados = await resposta.json();

        if (!resposta.ok) {
            throw new Error(dados.erro || "Nao foi possivel cadastrar o usuario.");
        }

        localStorage.setItem('idUsuario', dados.idUsuario);
        localStorage.setItem('nomeUsuario', dados.nome);
        window.location.href = `/Homepage/homepage.html?idUsuario=${dados.idUsuario}`;
    } catch (error) {
        erro.innerText = error.message;
        botao.disabled = false;
        botao.innerText = "Cadastrar e entrar";
    }
});
