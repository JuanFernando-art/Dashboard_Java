﻿
const API_BASE_URL = 'http://localhost:7000/api';

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
    
    if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.erro || `Erro na requisição: ${response.status}`);
    }

    return response;
}

/**
 * Atualiza o cabeçalho da sidebar para mostrar o nome da Loja
 * e configura o link de retorno para a homepage.
 */
async function carregarCabecalhoSidebar() {
    const idEmpreendimento = localStorage.getItem('idEmpreendimento');
    const labelDisplay = document.getElementById('usuarioLogado');
    const labelApoio = document.querySelector('.usuario-logado span');
    const linkCategorias = document.getElementById('linkCategorias');
    const linkAcao = document.getElementById('linkHomepage'); 

    if (!localStorage.getItem('authToken')) {
        logout();
        return;
    }

    // Altera o texto de apoio (ex: de "Bem-vindo" para "Loja:")
    if (labelApoio) labelApoio.innerText = "Empreendimento:";

    // Primeiro, tentamos mostrar o que está no cache para não ficar vazio
    const nomeCache = localStorage.getItem('nomeEmpreendimento');
    if (labelDisplay) labelDisplay.innerText = nomeCache || 'Carregando...';

    // Agora "puxamos do banco" via API para garantir que o nome esteja correto
    if (idEmpreendimento) {
        try {
            const response = await apiFetch(`${API_BASE_URL}/empreendimentos/${idEmpreendimento}`);
            if (response.ok) {
                const dados = await response.json();
                if (labelDisplay) labelDisplay.innerText = dados.nome;
                // Atualizamos o cache para futuras cargas rápidas
                localStorage.setItem('nomeEmpreendimento', dados.nome);
            }
        } catch (error) {
            console.error("Erro ao buscar nome do empreendimento:", error);
        }
    }

    // Transforma o link de Logout em Voltar
    if (linkAcao) {
        linkAcao.innerText = "Voltar para Homepage";
        linkAcao.href = "../Homepage/homepage.html";
        linkAcao.onclick = null; // Remove a chamada da função logout()
    }

    // Altera o nome na barra lateral para "Categorias Personalizadas"
    if (linkCategorias) {
        linkCategorias.innerText = "Categorias Personalizadas";
    }
}

carregarCabecalhoSidebar();

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
    ? `${API_BASE_URL}/produtos?idEmpreendimento=${idEmpreendimentoAtual}`
    : null;

async function carregarProdutos() {
    if (!produtosUrl) return;

    try {
        const resposta = await apiFetch(produtosUrl);
        const produtos = await resposta.json();

        if (!Array.isArray(produtos)) {
            console.error("Dados de produtos inválidos:", produtos);
            return;
        }

        const corpoTabela = document.getElementById('tabela-corpo');
        corpoTabela.innerHTML = "";

        console.log("Produtos carregados:", produtos);

        const htmlProdutos = produtos.map(p => {
            const porcentagem = (p.quantidade / p.quantidadeInicial) * 100;
            let classeDestaque = "";

            if (porcentagem <= 20) {
                classeDestaque = "estoque-critico";
            } else if (porcentagem <= 50) {
                classeDestaque = "estoque-alerta";
            }

            return `
    <tr class="${classeDestaque}">
        <td>${p.id}</td>
        <td>
            ${p.nome} ${p.categoriaNome ? `<br><small class="text-muted">(${p.categoriaNome})</small>` : ''}
        </td>
        <td>${p.quantidade} / ${p.quantidadeInicial}</td>
        <td>R$ ${p.precoVenda.toFixed(2)}</td>
        <td>
            <button class="btn-editar" data-id="${p.id}">Editar</button>
            <button onclick='deletarProduto(${p.id})'>Excluir</button>
        </td>
    </tr>
`;
        }).join('');

        corpoTabela.innerHTML = htmlProdutos;
        
        // Adiciona listeners para os botões de editar para evitar problemas com JSON no HTML
        document.querySelectorAll('.btn-editar').forEach(btn => {
            btn.onclick = () => {
                const id = btn.getAttribute('data-id');
                const produto = produtos.find(p => p.id == id);
                prepararEdicao(produto);
            };
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
    limparSelecaoCategoria();
}

function fecharModal() {
    document.getElementById('modalProduto').style.display = 'none';
    document.getElementById('formProduto').reset();
    document.getElementById('prodId').value = "";
}

async function deletarProduto(id) {
    if (confirm("Tem certeza que deseja excluir este produto?")) {
        await apiFetch(`${API_BASE_URL}/produtos/${id}?idEmpreendimento=${idEmpreendimentoAtual}`, { method: 'DELETE' });
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
    
    if (p.idCategoria) {
        configurarCategoriaEdicao(p.idCategoria);
    } else {
        limparSelecaoCategoria();
    }
    validarLimite();
}

async function configurarCategoriaEdicao(idCategoria) {
    const resp = await apiFetch(`${API_BASE_URL}/categorias`);
    const categorias = await resp.json();
    const caminho = montarCaminhoCategoria(idCategoria, categorias);
    definirCategoriaSelecionada(idCategoria, caminho);
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
        quantidadeInicial: parseInt(document.getElementById('quantidadeInicial').value),
        idCategoria: document.getElementById('prodIdCategoria').value ? parseInt(document.getElementById('prodIdCategoria').value) : null
    };

    if (id) produto.id = parseInt(id);

    await apiFetch(`${API_BASE_URL}/produtos`, {
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
        <div class="item-pdv">
            <div class="info-produto">
                <strong>${p.nome}</strong><br>
                <small>R$ ${p.precoVenda.toFixed(2)} | Est: ${p.quantidade}</small>
            </div>
            <div class="acoes-pdv">
                <input type="number" id="qtd-pdv-${p.id}" class="input-qtd-venda" value="1" min="1" max="${p.quantidade}">
                <button class="btn-primary" onclick='adicionarAoCarrinho(${JSON.stringify(p)})'>Adicionar</button>
            </div>
        </div>
    `).join('');
}

function filtrarPDV() {
    const termo = document.getElementById('buscaPDV').value.toLowerCase();
    const filtrados = todosOsProdutos.filter(p => p.nome.toLowerCase().includes(termo));
    renderizarListaProdutosPDV(filtrados);
}

function adicionarAoCarrinho(produto) {
    const inputQtd = document.getElementById(`qtd-pdv-${produto.id}`);
    const quantidadeDesejada = parseInt(inputQtd.value) || 0;

    if (quantidadeDesejada <= 0) return alert("Informe uma quantidade válida.");

    const itemNoCarrinho = carrinho.find(item => item.id === produto.id);
    const qtdAtualNoCarrinho = itemNoCarrinho ? itemNoCarrinho.quantidadeCarrinho : 0;

    if (qtdAtualNoCarrinho + quantidadeDesejada > produto.quantidade) {
        return alert("Limite de estoque atingido ou quantidade superior ao disponível!");
    }

    if (itemNoCarrinho) {
        itemNoCarrinho.quantidadeCarrinho += quantidadeDesejada;
    } else {
        carrinho.push({ ...produto, quantidadeCarrinho: quantidadeDesejada });
    }

    renderizarCarrinho();
    inputQtd.value = 1; // Reseta o input após adicionar
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
                <button class="btn-remover-carrinho" onclick="removerDoCarrinho(${index})">Remover</button>
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
        const resposta = await apiFetch(`${API_BASE_URL}/vendas?idEmpreendimento=${idEmpreendimentoAtual}`);
        const vendas = await resposta.json();
        const corpoVendas = document.getElementById('corpoVendas');
        corpoVendas.innerHTML = "";

        
        vendas.forEach(v => {
            const dateFormat = new Date (v.dataVenda);

            corpoVendas.innerHTML += `
            <tr>
            <td>${dateFormat.toLocaleDateString('pt-br')}</td>
            <td>${v.produtosVendidos}</td>
            <td>R$ ${v.total.toFixed(2)}</td>
            </tr>
            `;
        });
    } catch (erro) {
        console.error("Erro ao carregar vendas:", erro);
    }
}

function atualizarLinkAtivo(id) {
    document.querySelectorAll('.sidebar nav a').forEach(a => a.classList.remove('active'));
    const link = document.getElementById(id);
    if (link) link.classList.add('active');
}

function mostrarProdutos() {
    atualizarLinkAtivo('linkEstoque');
    toggleSecao('secaoProdutos');
}

function mostrarVendas() {
    atualizarLinkAtivo('linkVendas');
    toggleSecao('secaoVendas');
    carregarHistoricoVendas();
}

function mostrarCategorias() {
    atualizarLinkAtivo('linkCategorias');
    toggleSecao('secaoCategorias');
    carregarCategorias();
}

function mostrarFinanceiro() {
    atualizarLinkAtivo('linkFinanceiro');
    toggleSecao('secaoFinanceiro');
    carregarContasPagamento();
}

function toggleSecao(idAtiva) {
    const secoes = ['secaoProdutos', 'secaoVendas', 'secaoCategorias', 'secaoFinanceiro'];
    secoes.forEach(id => {
        const el = document.getElementById(id);
        if (el) el.style.display = (id === idAtiva) ? 'block' : 'none';
    });
}

async function carregarCategorias() {
    try {
        const resp = await apiFetch(`${API_BASE_URL}/categorias?idEmpreendimento=${idEmpreendimentoAtual}`);
        const categorias = await resp.json();
        const container = document.getElementById('listaCategoriasArvore');
        if (container) {
            container.innerHTML = renderizarArvore(categorias, null, 1);
        }
    } catch (e) { 
        console.error("Erro ao carregar categorias:", e);
        alert("Falha ao carregar categorias: " + e.message);
    }
}

function renderizarArvore(lista, paiId, nivel) {
    const filtradas = lista.filter(c => (paiId === null ? !c.idCategoriaPai : c.idCategoriaPai === paiId));
    if (filtradas.length === 0) return "";

    return filtradas.map(cat => {
        const id = cat.idCategoria;
        const temFilhos = lista.some(c => c.idCategoriaPai === id);
        return `
            <div class="categoria-item" id="cat-item-${id}">
                <div class="categoria-header">
                    <div class="categoria-info" onclick="document.getElementById('cat-item-${id}').classList.toggle('aberto')">
                        <span class="toggle-icon">${temFilhos ? '▶' : '•'}</span>
                        <span class="categoria-nome">${cat.nome}</span>
                    </div>
                    <div class="categoria-acoes">
                        ${nivel < 4 ? `<button class="btn-add-sub" onclick="abrirModalCategoria(null, ${id})">+</button>` : ''}
                        <button onclick='abrirModalCategoria(${JSON.stringify(cat)})'>Editar</button>
                        <button onclick="deletarCategoria(${id})">Excluir</button>
                    </div>
                </div>
                <div class="subcategorias-container">${renderizarArvore(lista, id, nivel + 1)}</div>
            </div>`;
    }).join('');
}

/** --- SELEÇÃO DE CATEGORIA PARA PRODUTO --- **/

async function abrirModalSelecaoCategoria() {
    document.getElementById('modalSelecaoCategoria').style.display = 'block';
    const resp = await apiFetch(`${API_BASE_URL}/categorias?idEmpreendimento=${idEmpreendimentoAtual}`);
    const categorias = await resp.json();
    const container = document.getElementById('listaSelecaoCategorias');
    container.innerHTML = renderizarArvoreSelecao(categorias, null, 1, categorias);
}

function fecharModalSelecaoCategoria() {
    document.getElementById('modalSelecaoCategoria').style.display = 'none';
}

function renderizarArvoreSelecao(lista, paiId, nivel, listaCompleta) {
    const filtradas = lista.filter(c => (paiId === null ? !c.idCategoriaPai : c.idCategoriaPai === paiId));
    return filtradas.map(cat => {
        const temFilhos = listaCompleta.some(c => c.idCategoriaPai === cat.idCategoria);
        const caminhoCompleto = montarCaminhoCategoria(cat.idCategoria, listaCompleta);
        
        return `
            <div class="categoria-item">
                <div class="categoria-header">
                    <div class="categoria-info" onclick="definirCategoriaSelecionada(${cat.idCategoria}, '${caminhoCompleto}')">
                        <span class="toggle-icon">${temFilhos ? '▶' : '•'}</span>
                        <span class="categoria-nome">${cat.nome}</span>
                    </div>
                </div>
                <div class="subcategorias-container aberto" style="display:block">
                    ${renderizarArvoreSelecao(listaCompleta, cat.idCategoria, nivel + 1, listaCompleta)}
                </div>
            </div>`;
    }).join('');
}

function montarCaminhoCategoria(id, lista) {
    let atual = lista.find(c => c.idCategoria === id);
    let caminho = [atual.nome];
    while (atual && atual.idCategoriaPai) {
        atual = lista.find(c => c.idCategoria === atual.idCategoriaPai);
        if (atual) caminho.unshift(atual.nome);
    }
    return caminho.join(' >> ');
}

function definirCategoriaSelecionada(id, texto) {
    document.getElementById('prodIdCategoria').value = id;
    document.getElementById('btnSelecionarCat').innerText = texto;
    fecharModalSelecaoCategoria();
}

function limparSelecaoCategoria() {
    document.getElementById('prodIdCategoria').value = "";
    document.getElementById('btnSelecionarCat').innerText = "Selecionar Categoria...";
}


function abrirModalCategoria(cat = null, paiId = null) {
    document.getElementById('modalCategoria').style.display = 'block';
    document.getElementById('catId').value = cat ? cat.idCategoria : "";
    document.getElementById('catPaiId').value = paiId || (cat ? cat.idCategoriaPai : "");
    document.getElementById('catNome').value = cat ? cat.nome : "";
    document.getElementById('modalCatTitulo').innerText = cat ? "Editar Categoria" : (paiId ? "Nova Subcategoria" : "Nova Categoria");
}

function fecharModalCategoria() { document.getElementById('modalCategoria').style.display = 'none'; }

document.getElementById('formCategoria').addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
        const cat = {
            idCategoria: document.getElementById('catId').value ? parseInt(document.getElementById('catId').value) : null,
            nome: document.getElementById('catNome').value,
            idCategoriaPai: document.getElementById('catPaiId').value ? parseInt(document.getElementById('catPaiId').value) : null,
            idEmpreendimento: parseInt(idEmpreendimentoAtual)
        };
        await apiFetch(`${API_BASE_URL}/categorias`, {
            method: cat.idCategoria ? 'PUT' : 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(cat)
        });
        fecharModalCategoria(); 
        carregarCategorias();
    } catch (erro) {
        console.error("Erro ao salvar categoria:", erro);
        alert("Erro ao salvar: " + erro.message);
    }
});

async function deletarCategoria(id) {
    if (confirm("Excluir esta categoria removerá todas as suas subcategorias. Continuar?")) {
        await apiFetch(`${API_BASE_URL}/categorias/${id}`, { method: 'DELETE' });
        carregarCategorias();
    }
}

function fecharModalPagamento() {
    document.getElementById('modalPagamento').style.display = 'none';
    document.getElementById('infoPix').style.display = 'none';
}

async function selecionarPagamento(metodo) {
    if (metodo === 'PIX') {
        try {
            const configResp = await apiFetch(`${API_BASE_URL}/config/pagamento?idEmpreendimento=${idEmpreendimentoAtual}`);
            const config = await configResp.json();
            document.getElementById('chavePixTexto').innerText = config.pixChave;
            document.getElementById('infoPix').style.display = 'block';
        } catch (e) {
            alert("Erro ao buscar chave PIX. Verifique a configuração.");
        }
    } else {
        await processarVendaFinal(metodo);
    }
} 

async function finalizarVenda() {
    if (carrinho.length === 0) return alert("O carrinho está vazio!");
    document.getElementById('modalPagamento').style.display = 'block';
}

async function processarVendaFinal(metodo) {
    const totalVenda = carrinho.reduce((acc, item) => acc + (item.precoVenda * item.quantidadeCarrinho), 0);
    const listaNomes = carrinho.map(item => `${item.nome} (${item.quantidadeCarrinho})`).join(", ");
    
    if (!confirm(`Confirmar recebimento de R$ ${totalVenda.toFixed(2)} via ${metodo}?`)) {
        return;
    }
    try {
        const vendaResponse = await apiFetch(`${API_BASE_URL}/vendas`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                idEmpreendimento: parseInt(idEmpreendimentoAtual),
                total: totalVenda,
                produtosVendidos: listaNomes,
                formaPagamento: metodo,
                itens: carrinho.map(item => ({
                    idProduto: item.id,
                    quantidade: item.quantidadeCarrinho,
                    precoUnitario: item.precoVenda,
                    precoCustoNoMomento: item.precoCusto
                }))
            })
        });

        if (!vendaResponse.ok) {
            throw new Error("Nao foi possivel registrar a venda.");
        }

        for (const item of carrinho) {
            await apiFetch(`${API_BASE_URL}/produtos/subtrair`, {
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
        fecharModalPagamento();
        await carregarProdutos();
        mostrarProdutos();
    } catch (error) {
        console.error("Erro na venda:", error);
        alert("Houve um erro ao processar a venda.");
    }
}

/* --- FUNÇÕES PARA GESTÃO DE CONTAS DE PAGAMENTO --- */

async function carregarContasPagamento() {
    try {
        const resposta = await apiFetch(`${API_BASE_URL}/contas-pagamento?idEmpreendimento=${idEmpreendimentoAtual}`);
        const contas = await resposta.json();
        const corpoTabela = document.getElementById('corpoContasPagamento');
        corpoTabela.innerHTML = "";

        if (!Array.isArray(contas) || contas.length === 0) {
            corpoTabela.innerHTML = `<tr><td colspan="7" style="text-align: center; color: #666;">Nenhuma conta de pagamento cadastrada.</td></tr>`;
            return;
        }

        contas.forEach(c => {
            corpoTabela.innerHTML += `
                <tr>
                    <td>${c.nomeBanco}</td>
                    <td>${c.agencia}</td>
                    <td>${c.conta}</td>
                    <td>${c.tipoConta}</td>
                    <td>${c.pixChave || 'N/A'}</td>
                    <td>${c.ativaConta ? '✅ Ativa' : '❌ Inativa'}</td>
                    <td>${c.ativaPix ? '✅ Ativa' : '❌ Inativa'}</td>
                    <td>
                        <button class="btn-editar" onclick='abrirModalContaPagamento(${JSON.stringify(c)})'>Editar</button>
                        <button onclick='deletarContaPagamento(${c.idConta})'>Excluir</button>
                    </td>
                </tr>
            `;
        });
    } catch (erro) {
        console.error("Erro ao carregar contas de pagamento:", erro);
        alert("Falha ao carregar contas de pagamento: " + erro.message);
    }
}

function abrirModalContaPagamento(conta = null) {
    document.getElementById('modalContaPagamento').style.display = 'block';
    document.getElementById('formContaPagamento').reset(); // Limpa o formulário
    document.getElementById('contaId').value = ""; // Garante que o ID esteja vazio para nova conta

    if (conta) {
        document.getElementById('modalContaPagamentoTitulo').innerText = "Editar Conta de Pagamento";
        document.getElementById('contaId').value = conta.idConta;
        document.getElementById('nomeBanco').value = conta.nomeBanco;
        document.getElementById('agencia').value = conta.agencia;
        document.getElementById('conta').value = conta.conta;
        document.getElementById('tipoConta').value = conta.tipoConta;
        document.getElementById('pixChave').value = conta.pixChave;
        document.getElementById('ativaConta').checked = conta.ativaConta;
        document.getElementById('ativaPix').checked = conta.ativaPix;
    } else {
        document.getElementById('modalContaPagamentoTitulo').innerText = "Cadastrar Conta de Pagamento";
        document.getElementById('ativaConta').checked = false; 
        document.getElementById('ativaPix').checked = false;
    }
}

function fecharModalContaPagamento() {
    document.getElementById('modalContaPagamento').style.display = 'none';
}

document.getElementById('formContaPagamento').addEventListener('submit', async (e) => {
    e.preventDefault();

    let senha = "";
    const idConta = document.getElementById('contaId').value;
    if (idConta) {
        senha = prompt("Confirme sua senha de usuário para salvar alterações financeiras:");
        if (!senha) return;
    }

    const conta = {
        idConta: idConta ? parseInt(idConta) : 0,
        nomeBanco: document.getElementById('nomeBanco').value,
        agencia: document.getElementById('agencia').value,
        conta: document.getElementById('conta').value,
        tipoConta: document.getElementById('tipoConta').value,
        pixChave: document.getElementById('pixChave').value,
        ativaConta: document.getElementById('ativaConta').checked,
        ativaPix: document.getElementById('ativaPix').checked,
        idEmpreendimento: parseInt(idEmpreendimentoAtual),
        senha: senha // Enviado para validação no Java
    };

    try {
        await apiFetch(`${API_BASE_URL}/contas-pagamento`, {
            method: conta.idConta ? 'PUT' : 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(conta)
        });
        alert("Conta de pagamento salva com sucesso!");
        fecharModalContaPagamento();
        carregarContasPagamento();
    } catch (erro) {
        console.error("Erro ao salvar conta de pagamento:", erro);
        alert("Erro ao salvar conta de pagamento: " + erro.message);
    }
});

async function deletarContaPagamento(idConta) {
    const senha = prompt("Confirme sua senha de usuário para EXCLUIR esta conta:");
    if (!senha) return;

    if (confirm("Esta ação não pode ser desfeita. Excluir conta?")) {
        try {
            await apiFetch(`${API_BASE_URL}/contas-pagamento/${idConta}?idEmpreendimento=${idEmpreendimentoAtual}`, { 
                method: 'DELETE',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ senha: senha })
            });
            alert("Conta de pagamento excluída com sucesso!");
            carregarContasPagamento();
        } catch (erro) {
            console.error("Erro ao excluir conta de pagamento:", erro);
            alert("Erro ao excluir conta de pagamento: " + erro.message);
        }
    }
}
