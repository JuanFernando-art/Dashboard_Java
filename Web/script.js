async function carregarProdutos() {
    try {
        const resposta = await fetch('http://localhost:7000/api/produtos');
        const produtos = await resposta.json();

        const corpoTabela = document.getElementById('tabela-corpo');
        corpoTabela.innerHTML = ""; // Limpa a tabela antes de preencher

        produtos.forEach(produto => {
            const linha = `
                <tr>
                    <td>${produto.id}</td>
                    <td>${produto.nome}</td>
                    <td>${produto.quantidade}</td>
                    <td>R$ ${produto.precoVenda.toFixed(2)}</td>
                    <td><button>Editar</button></td>
                </tr>
            `;
            corpoTabela.innerHTML += linha;
        });

    } catch (erro) {
        console.error("Erro ao carregar dados do Java:", erro);
        alert("Não foi possível carregar os produtos. O Java está rodando?");
    }
}

// Inicia a busca assim que a página carregar
carregarProdutos();