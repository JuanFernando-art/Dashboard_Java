﻿﻿﻿
const API_BASE_URL = 'http://localhost:7000/api';

// Componente para criação de modais de alerta, confirmação e solicitação de senha
const Dialogos = {
    _criarEstruturaBase(titulo, conteudo, acaoBotao, textoBotao = "Confirmar", tipo = "primary") {
        const modalId = 'modal-dinamico-' + Date.now();
        const html = `
            <div id="${modalId}" class="modal" style="display: block;">
                <div class="modal-content">
                    <span class="close" onclick="document.getElementById('${modalId}').remove()">&times;</span>
                    <h2 style="margin-bottom: 15px;">${titulo}</h2>
                    <div class="modal-body">${conteudo}</div>
                    <div class="modal-footer">
                        <button class="btn-secondary" onclick="document.getElementById('${modalId}').remove()">Cancelar</button>
                        <button class="btn-${tipo}" id="btn-confirmar-${modalId}">${textoBotao}</button>
                    </div>
                </div>
            </div>`;
        
        document.body.insertAdjacentHTML('beforeend', html);
        const modal = document.getElementById(modalId);
        const btn = document.getElementById(`btn-confirmar-${modalId}`);
        
        btn.onclick = () => {
            const input = modal.querySelector('input');
            const valor = input ? input.value : true;
            if (input && !valor) {
                input.style.borderColor = 'red';
                return;
            }
            acaoBotao(valor);
            modal.remove();
        };
    },

    alerta(mensagem, titulo = "Aviso") {
        this._criarEstruturaBase(titulo, `<p>${mensagem}</p>`, () => {}, "Entendido");
        setTimeout(() => {
            const modal = document.body.lastElementChild;
            modal.querySelector('.btn-secondary').remove();
        }, 10);
    },

    confirmar(mensagem, callback, titulo = "Confirmação") {
        this._criarEstruturaBase(titulo, `<p>${mensagem}</p>`, callback);
    },

    perguntaSenha(mensagem, callback, titulo = "Segurança") {
        const conteudo = `
            <p style="margin-bottom: 10px;">${mensagem}</p>
            <input type="password" id="campo-senha-dialogo" placeholder="Sua senha" 
                   style="width:100%; padding:10px; border:1px solid #ddd; border-radius:5px;">`;
        this._criarEstruturaBase(titulo, conteudo, callback, "Validar e Prosseguir", "primary");
        
        setTimeout(() => document.getElementById('campo-senha-dialogo').focus(), 50);
    }
};

// Remove os dados da sessão e redireciona para o login
function logout() {
    localStorage.removeItem('authToken');
    localStorage.removeItem('idUsuario');
    localStorage.removeItem('nomeUsuario');
    localStorage.removeItem('emailUsuario');
    localStorage.removeItem('idEmpreendimento');
    window.location.href = '/index.html';
}

// Realiza chamadas à API injetando o token de autorização
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

// Configura os elementos visuais da sidebar baseados no empreendimento atual
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

    if (labelApoio) labelApoio.innerText = "Empreendimento:";

    const nomeCache = localStorage.getItem('nomeEmpreendimento');
    if (labelDisplay) labelDisplay.innerText = nomeCache || 'Carregando...';

    if (idEmpreendimento) {
        try {
            const response = await apiFetch(`${API_BASE_URL}/empreendimentos/${idEmpreendimento}`);
            if (response.ok) {
                const dados = await response.json();
                if (labelDisplay) labelDisplay.innerText = dados.nome;
                localStorage.setItem('nomeEmpreendimento', dados.nome);
            }
        } catch (error) {
            console.error("Erro ao buscar nome do empreendimento:", error);
        }
    }

    if (linkAcao) {
        linkAcao.innerText = "Voltar para Homepage";
        linkAcao.href = "../Homepage/homepage.html";
        linkAcao.onclick = null; 
    }

    if (linkCategorias) {
        linkCategorias.innerText = "Categorias Personalizadas";
    }
}

carregarCabecalhoSidebar();

// Configura o comportamento do botão de retorno
function configurarRetornoHomepage() {
    const linkHomepage = document.getElementById('linkHomepage');
    if (!linkHomepage) return;

    linkHomepage.addEventListener('click', (event) => {
        event.preventDefault();
        window.location.href = '../Homepage/homepage.html';
    });
}

configurarRetornoHomepage();

// Valida se o ID do empreendimento está presente na sessão
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

// Busca e renderiza a lista de produtos na tabela principal
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

carregarProdutos();

// Controla a abertura do modal de produtos
function abrirModal() {
    document.getElementById('modalTitulo').innerText = "Cadastrar Produto";
    document.getElementById('modalProduto').style.display = 'block';
    limparSelecaoCategoria();
}

// Controla o fechamento do modal de produtos
function fecharModal() {
    document.getElementById('modalProduto').style.display = 'none';
    document.getElementById('formProduto').reset();
    document.getElementById('prodId').value = "";
}

// Solicita confirmação e remove um produto
async function deletarProduto(id) {
    Dialogos.confirmar("Tem certeza que deseja excluir este produto? Esta ação é irreversível.", async () => {
        await apiFetch(`${API_BASE_URL}/produtos/${id}?idEmpreendimento=${idEmpreendimentoAtual}`, { method: 'DELETE' });
        carregarProdutos();
    }, "Excluir Produto");
}

// Carrega os dados de um produto no formulário para edição
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

// Localiza e exibe o caminho hierárquico da categoria selecionada na edição
async function configurarCategoriaEdicao(idCategoria) {
    const resp = await apiFetch(`${API_BASE_URL}/categorias`);
    const categorias = await resp.json();
    const caminho = montarCaminhoCategoria(idCategoria, categorias);
    definirCategoriaSelecionada(idCategoria, caminho);
}

// Processa o envio do formulário de produto (Criação ou Atualização)
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

// Filtra as linhas da tabela de produtos por texto e status de estoque
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

// Valida se a quantidade atual não excede o limite inicial configurado
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

// Inicializa o modal de Ponto de Venda e carrega o catálogo de produtos
async function abrirModalPDV() {
    document.getElementById('modalPDV').style.display = 'block';
    carrinho = [];
    renderizarCarrinho();

    const resp = await apiFetch(produtosUrl);
    todosOsProdutos = await resp.json();
    renderizarListaProdutosPDV(todosOsProdutos);
}

// Gera o HTML da lista de produtos selecionáveis no PDV
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

// Realiza a busca textual no catálogo do PDV
function filtrarPDV() {
    const termo = document.getElementById('buscaPDV').value.toLowerCase();
    const filtrados = todosOsProdutos.filter(p => p.nome.toLowerCase().includes(termo));
    renderizarListaProdutosPDV(filtrados);
}

// Adiciona um produto e sua quantidade ao carrinho virtual
function adicionarAoCarrinho(produto) {
    const inputQtd = document.getElementById(`qtd-pdv-${produto.id}`);
    const quantidadeDesejada = parseInt(inputQtd.value) || 0;

    if (quantidadeDesejada <= 0) return Dialogos.alerta("Informe uma quantidade válida.");

    const itemNoCarrinho = carrinho.find(item => item.id === produto.id);
    const qtdAtualNoCarrinho = itemNoCarrinho ? itemNoCarrinho.quantidadeCarrinho : 0;

    if (qtdAtualNoCarrinho + quantidadeDesejada > produto.quantidade) {
        return Dialogos.alerta("Limite de estoque atingido ou quantidade superior ao disponível!");
    }

    if (itemNoCarrinho) {
        itemNoCarrinho.quantidadeCarrinho += quantidadeDesejada;
    } else {
        carrinho.push({ ...produto, quantidadeCarrinho: quantidadeDesejada });
    }

    renderizarCarrinho();
    inputQtd.value = 1; 
}

// Renderiza os itens do carrinho e calcula o valor total da venda
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

// Remove um item específico do carrinho pelo índice
function removerDoCarrinho(index) {
    carrinho.splice(index, 1);
    renderizarCarrinho();
}

// Busca e exibe o histórico de vendas realizadas
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

// Atualiza a classe visual de link ativo na navegação lateral
function atualizarLinkAtivo(id) {
    document.querySelectorAll('.sidebar nav a').forEach(a => a.classList.remove('active'));
    const link = document.getElementById(id);
    if (link) link.classList.add('active');
}

// Exibe a seção de gestão de estoque
function mostrarProdutos() {
    atualizarLinkAtivo('linkEstoque');
    toggleSecao('secaoProdutos');
}

// Exibe a seção de histórico de vendas
function mostrarVendas() {
    atualizarLinkAtivo('linkVendas');
    toggleSecao('secaoVendas');
    carregarHistoricoVendas();
}

// Exibe a seção de categorias
function mostrarCategorias() {
    atualizarLinkAtivo('linkCategorias');
    toggleSecao('secaoCategorias');
    carregarCategorias();
}

// Exibe a seção financeira e contas de pagamento
function mostrarFinanceiro() {
    atualizarLinkAtivo('linkFinanceiro');
    toggleSecao('secaoFinanceiro');
    carregarContasPagamento();
}

// Alterna a visibilidade entre os diferentes painéis do dashboard
function toggleSecao(idAtiva) {
    const secoes = ['secaoProdutos', 'secaoVendas', 'secaoCategorias', 'secaoFinanceiro'];
    secoes.forEach(id => {
        const el = document.getElementById(id);
        if (el) el.style.display = (id === idAtiva) ? 'block' : 'none';
    });
}

// Carrega e renderiza a árvore de categorias
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
        Dialogos.alerta("Falha ao carregar categorias: " + e.message);
    }
}

// Função recursiva para gerar a estrutura visual da árvore de categorias
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

// Abre o modal para seleção de categoria no cadastro de produtos
async function abrirModalSelecaoCategoria() {
    document.getElementById('modalSelecaoCategoria').style.display = 'block';
    const resp = await apiFetch(`${API_BASE_URL}/categorias?idEmpreendimento=${idEmpreendimentoAtual}`);
    const categorias = await resp.json();
    const container = document.getElementById('listaSelecaoCategorias');
    container.innerHTML = renderizarArvoreSelecao(categorias, null, 1, categorias);
}

// Fecha o modal de seleção de categoria
function fecharModalSelecaoCategoria() {
    document.getElementById('modalSelecaoCategoria').style.display = 'none';
}

// Função recursiva para renderizar a árvore no modal de seleção
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

// Reconstrói a string de hierarquia (Ex: Cat A >> Sub B) de uma categoria
function montarCaminhoCategoria(id, lista) {
    let atual = lista.find(c => c.idCategoria === id);
    let caminho = [atual.nome];
    while (atual && atual.idCategoriaPai) {
        atual = lista.find(c => c.idCategoria === atual.idCategoriaPai);
        if (atual) caminho.unshift(atual.nome);
    }
    return caminho.join(' >> ');
}

// Atribui a categoria escolhida ao produto no formulário
function definirCategoriaSelecionada(id, texto) {
    document.getElementById('prodIdCategoria').value = id;
    document.getElementById('btnSelecionarCat').innerText = texto;
    fecharModalSelecaoCategoria();
}

// Remove a categoria associada a um produto no formulário
function limparSelecaoCategoria() {
    document.getElementById('prodIdCategoria').value = "";
    document.getElementById('btnSelecionarCat').innerText = "Selecionar Categoria...";
}

// Controla a abertura do modal para criação ou edição de categorias
function abrirModalCategoria(cat = null, paiId = null) {
    document.getElementById('modalCategoria').style.display = 'block';
    document.getElementById('catId').value = cat ? cat.idCategoria : "";
    document.getElementById('catPaiId').value = paiId || (cat ? cat.idCategoriaPai : "");
    document.getElementById('catNome').value = cat ? cat.nome : "";
    document.getElementById('modalCatTitulo').innerText = cat ? "Editar Categoria" : (paiId ? "Nova Subcategoria" : "Nova Categoria");
}

// Fecha o modal de categorias
function fecharModalCategoria() { document.getElementById('modalCategoria').style.display = 'none'; }

// Processa o envio do formulário de categorias
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
        Dialogos.alerta("Erro ao salvar: " + erro.message);
    }
});

// Solicita confirmação e remove uma categoria do sistema
async function deletarCategoria(id) {
    Dialogos.confirmar("Excluir esta categoria removerá todas as suas subcategorias. Continuar?", async () => {
        await apiFetch(`${API_BASE_URL}/categorias/${id}`, { method: 'DELETE' });
        carregarCategorias();
    }, "Excluir Categoria");
}

// Controla o fechamento do modal de formas de pagamento
function fecharModalPagamento() {
    document.getElementById('modalPagamento').style.display = 'none';
    document.getElementById('infoPix').style.display = 'none';
}

// Gerencia a seleção da forma de pagamento e exibe dados de PIX se necessário
async function selecionarPagamento(metodo) {
    if (metodo === 'PIX') {
        try {
            const configResp = await apiFetch(`${API_BASE_URL}/config/pagamento?idEmpreendimento=${idEmpreendimentoAtual}`);
            const config = await configResp.json();
            document.getElementById('chavePixTexto').innerText = config.pixChave;
            document.getElementById('infoPix').style.display = 'block';
        } catch (e) {
            Dialogos.alerta("Erro ao buscar chave PIX. Verifique se existe uma conta com PIX ativo.");
        }
    } else {
        await processarVendaFinal(metodo);
    }
} 

// Inicia o processo de finalização de venda validando o carrinho
async function finalizarVenda() {
    if (carrinho.length === 0) return Dialogos.alerta("O carrinho está vazio!");
    document.getElementById('modalPagamento').style.display = 'block';
}

// Executa as chamadas de API para registrar a venda e abater o estoque
async function processarVendaFinal(metodo) {
    const totalVenda = carrinho.reduce((acc, item) => acc + (item.precoVenda * item.quantidadeCarrinho), 0);
    const listaNomes = carrinho.map(item => `${item.nome} (${item.quantidadeCarrinho})`).join(", ");
    
    Dialogos.confirmar(`Confirmar recebimento de R$ ${totalVenda.toFixed(2)} via ${metodo}?`, async () => {
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

            if (!vendaResponse.ok) throw new Error("Não foi possível registrar a venda.");

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

            Dialogos.alerta("Venda realizada com sucesso!");
            carrinho = [];
            document.getElementById('modalPDV').style.display = 'none';
            fecharModalPagamento();
            await carregarProdutos();
            mostrarProdutos();
        } catch (error) {
            console.error("Erro na venda:", error);
            Dialogos.alerta("Houve um erro ao processar a venda.");
        }
    }, "Concluir Venda");
}

// Busca e renderiza as contas de pagamento do empreendimento
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
        Dialogos.alerta("Falha ao carregar contas de pagamento: " + erro.message);
    }
}

// Controla a abertura do modal de cadastro/edição de contas financeiras
function abrirModalContaPagamento(conta = null) {
    document.getElementById('modalContaPagamento').style.display = 'block';
    document.getElementById('formContaPagamento').reset(); 
    document.getElementById('contaId').value = ""; 

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

// Fecha o modal de contas de pagamento
function fecharModalContaPagamento() {
    document.getElementById('modalContaPagamento').style.display = 'none';
}

// Processa o salvamento de contas financeiras exigindo senha para edições
document.getElementById('formContaPagamento').addEventListener('submit', async (e) => {
    e.preventDefault();

    const idConta = document.getElementById('contaId').value;
    
    const salvarDados = async (senha = "") => {
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
            senha: senha
        };

        try {
            await apiFetch(`${API_BASE_URL}/contas-pagamento`, {
                method: conta.idConta ? 'PUT' : 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(conta)
            });
            Dialogos.alerta("Conta de pagamento salva com sucesso!");
            fecharModalContaPagamento();
            carregarContasPagamento();
        } catch (erro) {
            Dialogos.alerta("Erro ao salvar conta: " + erro.message);
        }
    };

    if (idConta) {
        Dialogos.perguntaSenha("Confirme sua senha para salvar alterações financeiras:", salvarDados);
    } else {
        salvarDados();
    }
});

// Solicita senha e confirmação para exclusão de conta financeira
async function deletarContaPagamento(idConta) {
    Dialogos.perguntaSenha("Informe sua senha para excluir esta conta permanentemente:", (senha) => {
        Dialogos.confirmar("Esta ação não pode ser desfeita. Excluir conta?", async () => {
            try {
                await apiFetch(`${API_BASE_URL}/contas-pagamento/${idConta}?idEmpreendimento=${idEmpreendimentoAtual}`, { 
                    method: 'DELETE',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ senha: senha })
                });
                Dialogos.alerta("Conta excluída com sucesso!");
                carregarContasPagamento();
            } catch (erro) {
                Dialogos.alerta("Erro ao excluir conta: " + erro.message);
            }
        }, "Confirmar Exclusão");
    });
}
