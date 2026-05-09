function logout() {
    localStorage.removeItem('authToken');
    localStorage.removeItem('idUsuario');
    localStorage.removeItem('nomeUsuario');
    localStorage.removeItem('emailUsuario');
    localStorage.removeItem('idEmpreendimento');
    window.location.href = '/index.html';
}

async function apiFetch(url, options = {}) {
    const token = localStorage.getItem('authToken');
    if (!token) {
        logout();
        throw new Error('Sessao expirada.');
    }

    const headers = {
        ...(options.headers || {}),
        Authorization: `Bearer ${token}`
    };

    const response = await fetch(url, { ...options, headers });
    if (response.status === 401) {
        logout();
        throw new Error('Sessao expirada.');
    }

    return response;
}

function carregarUsuarioDaSessao() {
    const nomeUsuario = localStorage.getItem('nomeUsuario');
    const emailUsuario = localStorage.getItem('emailUsuario');
    const labelUsuario = document.getElementById('usuarioLogado');

    if (!localStorage.getItem('authToken')) {
        logout();
        return;
    }

    if (labelUsuario) {
        labelUsuario.innerText = nomeUsuario || emailUsuario || 'Usuario logado';
    }
}

carregarUsuarioDaSessao();

function configurarRetornoHomepage() {
    const linkHomepage = document.getElementById('linkHomepage');
    if (!linkHomepage) return;

    linkHomepage.addEventListener('click', (event) => {
        event.preventDefault();
        window.location.href = '../Homepage/homepage.html';
    });
}

configurarRetornoHomepage();

function carregarEmpreendimentoDaSessao() {
    const idEmpreendimento = localStorage.getItem('idEmpreendimento');
    if (!idEmpreendimento) {
        window.location.href = '../Homepage/homepage.html';
        return null;
    }

    return idEmpreendimento;
}

const idEmpreendimentoAtual = carregarEmpreendimentoDaSessao();
const produtosUrl = idEmpreendimentoAtual
    ? `http://localhost:7000/api/produtos?idEmpreendimento=${idEmpreendimentoAtual}`
    : null;

async function carregarProdutos() {
    if (!produtosUrl) return;

    try {
        const resposta = await apiFetch(produtosUrl);
        const produtos = await resposta.json();

        const corpoTabela = document.getElementById('tabela-corpo');
        corpoTabela.innerHTML = "";

        console.log("Produtos carregados:", produtos);

        produtos.forEach(p => {
            // LÃ³gica das cores baseada na sua regra:
            const porcentagem = (p.quantidade / p.quantidadeInicial) * 100;
            let classeDestaque = ""; // Use classe em vez de style direto

            if (porcentagem <= 20) {
                classeDestaque = "estoque-critico";
            } else if (porcentagem <= 50) {
                classeDestaque = "estoque-alerta";
            }

            const linha = `
    <tr class="${classeDestaque}"> <td>${p.id}</td>
        <td>${p.nome}</td>
        <td>${p.quantidade} / ${p.quantidadeInicial}</td>
        <td>R$ ${p.precoVenda.toFixed(2)}</td>
        <td>
            <button onclick='prepararEdicao(${JSON.stringify(p)})'>Editar</button>
            <button onclick='deletarProduto(${p.id})'>Excluir</button>
        </td>
    </tr>
`;
            corpoTabela.innerHTML += linha;
        });

    } catch (erro) {
        console.error("Erro ao carregar dados do Java:", erro);
    }
}

// Inicia a busca
carregarProdutos();

function abrirModal() {
    document.getElementById('modalTitulo').innerText = "Cadastrar Produto";
    document.getElementById('modalProduto').style.display = 'block';
}

function fecharModal() {
    document.getElementById('modalProduto').style.display = 'none';
    document.getElementById('formProduto').reset();
    document.getElementById('prodId').value = "";
}

async function deletarProduto(id) {
    if (confirm("Tem certeza que deseja excluir este produto?")) {
        await apiFetch(`http://localhost:7000/api/produtos/${id}?idEmpreendimento=${idEmpreendimentoAtual}`, { method: 'DELETE' });
        carregarProdutos();
    }
}

async function prepararEdicao(p) {
    abrirModal();
    document.getElementById('modalTitulo').innerText = "Editar Produto";
    document.getElementById('prodId').value = p.id;
    document.getElementById('nome').value = p.nome;
    document.getElementById('precoCusto').value = p.precoCusto;
    document.getElementById('precoVenda').value = p.precoVenda;
    document.getElementById('quantidade').value = p.quantidade;
    document.getElementById('quantidadeInicial').value = p.quantidadeInicial;
    validarLimite();
}

document.getElementById('formProduto').addEventListener('submit', async (e) => {
    e.preventDefault();

    const id = document.getElementById('prodId').value;
    const produto = {
        nome: document.getElementById('nome').value,
        idEmpreendimento: parseInt(idEmpreendimentoAtual),
        precoCusto: parseFloat(document.getElementById('precoCusto').value),
        precoVenda: parseFloat(document.getElementById('precoVenda').value),
        quantidade: parseInt(document.getElementById('quantidade').value),
        quantidadeInicial: parseInt(document.getElementById('quantidadeInicial').value)
    };

    if (id) produto.id = parseInt(id);

    await apiFetch('http://localhost:7000/api/produtos', {
        method: id ? 'PUT' : 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(produto)
    });

    fecharModal();
    carregarProdutos();
});

function filtrarTabela() {
    const termo = document.getElementById('inputBusca').value.toLowerCase();
    const filtroStatus = document.getElementById('filtroEstoque').value;
    const linhas = document.querySelectorAll('#tabela-corpo tr');

    linhas.forEach(linha => {
        const texto = linha.innerText.toLowerCase();
        const possuiTermo = texto.includes(termo);
        const isCritico = linha.classList.contains('estoque-critico');
        const isAlerta = linha.classList.contains('estoque-alerta');

        let atendeFiltro = true;
        if (filtroStatus === 'critico') atendeFiltro = isCritico;
        else if (filtroStatus === 'alerta') atendeFiltro = isAlerta;

        linha.style.display = (possuiTermo && atendeFiltro) ? "" : "none";
    });
}

function validarLimite() {
    const qtd = parseInt(document.getElementById('quantidade').value) || 0;
    const limite = parseInt(document.getElementById('quantidadeInicial').value) || 0;
    const btn = document.querySelector('#formProduto .btn-save');
    const erro = document.getElementById('erroLimite');

    if (qtd > limite) {
        btn.disabled = true;
        btn.style.opacity = "0.5";
        erro.style.display = "block";
    } else {
        btn.disabled = false;
        btn.style.opacity = "1";
        erro.style.display = "none";
    }
}

let carrinho = [];
let todosOsProdutos = [];

async function abrirModalPDV() {
    document.getElementById('modalPDV').style.display = 'block';
    carrinho = [];
    renderizarCarrinho();

    const resp = await apiFetch(produtosUrl);
    todosOsProdutos = await resp.json();
    renderizarListaProdutosPDV(todosOsProdutos);
}

function renderizarListaProdutosPDV(lista) {
    const div = document.getElementById('listaProdutosPDV');
    div.innerHTML = lista.map(p => `
        <div class="item-pdv" onclick='adicionarAoCarrinho(${JSON.stringify(p)})'>
            <span>${p.nome}</span>
            <span>R$ ${p.precoVenda.toFixed(2)} | Est: ${p.quantidade}</span>
        </div>
    `).join('');
}

function filtrarPDV() {
    const termo = document.getElementById('buscaPDV').value.toLowerCase();
    const filtrados = todosOsProdutos.filter(p => p.nome.toLowerCase().includes(termo));
    renderizarListaProdutosPDV(filtrados);
}

function adicionarAoCarrinho(produto) {
    const itemNoCarrinho = carrinho.find(item => item.id === produto.id);

    if (itemNoCarrinho) {
        if (itemNoCarrinho.quantidadeCarrinho < produto.quantidade) {
            itemNoCarrinho.quantidadeCarrinho++;
        } else {
            alert("Limite de estoque atingido!");
        }
    } else if (produto.quantidade > 0) {
        carrinho.push({ ...produto, quantidadeCarrinho: 1 });
    } else {
        alert("Produto sem estoque!");
    }

    renderizarCarrinho();
}

function renderizarCarrinho() {
    const container = document.getElementById('itensCarrinho');
    let totalVenda = 0;

    container.innerHTML = carrinho.map((item, index) => {
        totalVenda += item.precoVenda * item.quantidadeCarrinho;
        return `
            <div class="linha-carrinho">
                <span>${item.nome} x${item.quantidadeCarrinho}</span>
                <span>R$ ${(item.precoVenda * item.quantidadeCarrinho).toFixed(2)}</span>
                <button onclick="removerDoCarrinho(${index})">Remover</button>
            </div>
        `;
    }).join('');

    document.getElementById('totalVenda').innerText = `Total: R$ ${totalVenda.toFixed(2)}`;
}

function removerDoCarrinho(index) {
    carrinho.splice(index, 1);
    renderizarCarrinho();
}

async function carregarHistoricoVendas() {
    try {
        const resposta = await apiFetch(`http://localhost:7000/api/vendas?idEmpreendimento=${idEmpreendimentoAtual}`);
        const vendas = await resposta.json();
        const corpoVendas = document.getElementById('corpoVendas');
        corpoVendas.innerHTML = "";

        vendas.forEach(v => {
            corpoVendas.innerHTML += `
                <tr>
                    <td>${v.dataVenda}</td>
                    <td>${v.produtosVendidos}</td>
                    <td>R$ ${v.total.toFixed(2)}</td>
                </tr>
            `;
        });
    } catch (erro) {
        console.error("Erro ao carregar vendas:", erro);
    }
}

function mostrarProdutos() {
    document.getElementById('secaoProdutos').style.display = 'block';
    document.getElementById('secaoVendas').style.display = 'none';
}

function mostrarVendas() {
    document.getElementById('secaoProdutos').style.display = 'none';
    document.getElementById('secaoVendas').style.display = 'block';
    carregarHistoricoVendas();
}

async function finalizarVenda() {
    if (carrinho.length === 0) return alert("O carrinho esta vazio!");

    const totalVenda = carrinho.reduce((acc, item) => acc + (item.precoVenda * item.quantidadeCarrinho), 0);
    const listaNomes = carrinho.map(item => `${item.nome} (${item.quantidadeCarrinho})`).join(", ");
    const itens = carrinho.map(item => ({
        idProduto: item.id,
        quantidade: item.quantidadeCarrinho,
        precoUnitario: item.precoVenda,
        precoCustoNoMomento: item.precoCusto
    }));

    try {
        const vendaResponse = await apiFetch('http://localhost:7000/api/vendas', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                idEmpreendimento: parseInt(idEmpreendimentoAtual),
                total: totalVenda,
                produtosVendidos: listaNomes,
                itens
            })
        });

        if (!vendaResponse.ok) {
            throw new Error("Nao foi possivel registrar a venda.");
        }

        for (const item of carrinho) {
            await apiFetch('http://localhost:7000/api/produtos/subtrair', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    id: item.id,
                    quantidadeVendida: item.quantidadeCarrinho,
                    idEmpreendimento: parseInt(idEmpreendimentoAtual)
                })
            });
        }

        alert("Venda realizada com sucesso!");
        carrinho = [];
        document.getElementById('modalPDV').style.display = 'none';
        await carregarProdutos();
        mostrarProdutos();
    } catch (error) {
        console.error("Erro na venda:", error);
        alert("Houve um erro ao processar a venda.");
    }
}
