async function carregarProdutos() {
    try {
        const resposta = await fetch('http://localhost:7000/api/produtos');
        const produtos = await resposta.json();

        const corpoTabela = document.getElementById('tabela-corpo');
        corpoTabela.innerHTML = "";

        produtos.forEach(p => {
            // Lógica das cores baseada na sua regra:
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
        <td>${p.categoria}</td>
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

// --- FUNÇÕES DO MODAL ---
function abrirModal() {
    document.getElementById('modalProduto').style.display = 'block';
}

function fecharModal() {
    document.getElementById('modalProduto').style.display = 'none';
    document.getElementById('formProduto').reset();
    document.getElementById('prodId').value = "";
}

// --- FUNÇÃO PARA DELETAR ---
async function deletarProduto(id) {
    if (confirm("Tem certeza que deseja excluir este produto?")) {
        await fetch(`http://localhost:7000/api/produtos/${id}`, { method: 'DELETE' });
        carregarProdutos(); // Atualiza a lista
    }
}

// --- PREPARAR EDIÇÃO (É a sua editarProduto melhorada) ---
function prepararEdicao(p) {
    abrirModal();
    document.getElementById('modalTitulo').innerText = "Editar Produto";
    document.getElementById('prodId').value = p.id;
    document.getElementById('nome').value = p.nome;
    document.getElementById('categoria').value = p.categoria;
    document.getElementById('precoCusto').value = p.precoCusto;
    document.getElementById('precoVenda').value = p.precoVenda;
    document.getElementById('quantidade').value = p.quantidade;
    document.getElementById('quantidadeInicial').value = p.quantidadeInicial;
    validarLimite();
}

// --- SALVAR (POST / PUT) ---
document.getElementById('formProduto').addEventListener('submit', async (e) => {
    e.preventDefault();

    const id = document.getElementById('prodId').value;

    const produto = {
        nome: document.getElementById('nome').value,
        categoria: document.getElementById('categoria').value,
        precoCusto: parseFloat(document.getElementById('precoCusto').value),
        precoVenda: parseFloat(document.getElementById('precoVenda').value),
        quantidade: parseInt(document.getElementById('quantidade').value),
        quantidadeInicial: parseInt(document.getElementById('quantidadeInicial').value) // <--- CONFIRA ESTE NOME
    };

    // Se tiver ID, incluímos no objeto para o Java saber quem atualizar
    if (id) produto.id = parseInt(id);

    const metodo = id ? 'PUT' : 'POST';

    await fetch('http://localhost:7000/api/produtos', {
        method: id ? 'PUT' : 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(produto)
    });

    fecharModal();
    carregarProdutos();
});

//função da pesquisa
function filtrarTabela() {
    const termo = document.getElementById('inputBusca').value.toLowerCase();
    const filtroStatus = document.getElementById('filtroEstoque').value;
    const linhas = document.querySelectorAll('#tabela-corpo tr');

    linhas.forEach(linha => {
        const texto = linha.innerText.toLowerCase();
        const possuiTermo = texto.includes(termo);

        // Verifica se a linha tem a classe de alerta ou crítico
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
    const btn = document.querySelector('.btn-save');
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

//Lógica do carrinho de compras

let carrinho = [];
let todosOsProdutos = []; // Para busca rápida no PDV

// Abre o Modal de Venda e limpa o carrinho anterior
async function abrirModalPDV() {
    document.getElementById('modalPDV').style.display = 'block';
    carrinho = [];
    renderizarCarrinho();

    // Busca produtos atualizados do estoque para o vendedor escolher
    const resp = await fetch('http://localhost:7000/api/produtos');
    todosOsProdutos = await resp.json();
    renderizarListaProdutosPDV(todosOsProdutos);
}

// Renderiza a lista de produtos disponíveis para clicar
function renderizarListaProdutosPDV(lista) {
    const div = document.getElementById('listaProdutosPDV');
    div.innerHTML = lista.map(p => `
        <div class="item-pdv" onclick='adicionarAoCarrinho(${JSON.stringify(p)})'>
            <span>${p.nome}</span>
            <span>R$ ${p.precoVenda.toFixed(2)} | Est: ${p.quantidade}</span>
        </div>
    `).join('');
}

// Adiciona o item selecionado ao carrinho
function adicionarAoCarrinho(produto) {
    const itemNoCarrinho = carrinho.find(item => item.id === produto.id);

    if (itemNoCarrinho) {
        if (itemNoCarrinho.quantidadeCarrinho < produto.quantidade) {
            itemNoCarrinho.quantidadeCarrinho++;
        } else {
            alert("Limite de estoque atingido!");
        }
    } else {
        if (produto.quantidade > 0) {
            carrinho.push({ ...produto, quantidadeCarrinho: 1 });
        } else {
            alert("Produto sem estoque!");
        }
    }
    renderizarCarrinho();
}

async function carregarHistoricoVendas() {
    try {
        const resposta = await fetch('http://localhost:7000/api/vendas');
        const vendas = await resposta.json();

        const corpoVendas = document.getElementById('corpoVendas');
        corpoVendas.innerHTML = "";

        vendas.forEach(v => {
            const linha = `
                <tr>
                    <td>${v.dataVenda}</td>
                    <td>${v.produtosVendidos}</td>
                    <td>R$ ${v.total.toFixed(2)}</td>
                </tr>
            `;
            corpoVendas.innerHTML += linha;
        });
    } catch (erro) {
        console.error("Erro ao carregar vendas:", erro);
    }
}
// Atualiza o visual do carrinho e o valor total
function renderizarCarrinho() {
    const container = document.getElementById('itensCarrinho');
    let totalVenda = 0;

    container.innerHTML = carrinho.map((item, index) => {
        totalVenda += item.precoVenda * item.quantidadeCarrinho;
        return `
            <div class="linha-carrinho">
                <span>${item.nome} x${item.quantidadeCarrinho}</span>
                <span>R$ ${(item.precoVenda * item.quantidadeCarrinho).toFixed(2)}</span>
                <button onclick="removerDoCarrinho(${index})">🗑️</button>
            </div>
        `;
    }).join('');

    document.getElementById('totalVenda').innerText = `Total: R$ ${totalVenda.toFixed(2)}`;
}

function removerDoCarrinho(index) {
    carrinho.splice(index, 1);
    renderizarCarrinho();
}
async function finalizarVenda() {
    if (carrinho.length === 0) return alert("Carrinho vazio!");

    // 1. Enviar para o Java salvar a Venda
    // 2. Para cada item no carrinho, chamar a rota PUT do Java para diminuir o estoque
    for (let item of carrinho) {
        item.quantidade = item.quantidadeOriginalDoBanco - item.quantidade;
        // Aqui chamamos o seu ProdutoDAO.atualizar() via fetch
    }

    alert("Venda realizada com sucesso!");
    carrinho = [];
    fecharModalPDV();
    carregarProdutos(); // Atualiza a tabela de estoque
}

function mostrarProdutos() {
    document.getElementById('secaoProdutos').style.display = 'block';
    document.getElementById('secaoVendas').style.display = 'none';
}

function mostrarVendas() {
    document.getElementById('secaoProdutos').style.display = 'none';
    document.getElementById('secaoVendas').style.display = 'block';
    carregarHistoricoVendas(); // Carrega os registros do banco
}

async function finalizarVenda() {
    if (carrinho.length === 0) return alert("O carrinho está vazio!");

    const totalVenda = carrinho.reduce((acc, item) => acc + (item.precoVenda * item.quantidadeCarrinho), 0);
    const listaNomes = carrinho.map(item => `${item.nome} (${item.quantidadeCarrinho})`).join(", ");

    try {
        // 1. Salva o registro da venda no histórico
        await fetch('http://localhost:7000/api/vendas', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ total: totalVenda, produtosVendidos: listaNomes })
        });

        // 2. Loop para baixar o estoque de cada produto vendido
        for (const item of carrinho) {
            await fetch('http://localhost:7000/api/produtos/subtrair', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ id: item.id, quantidadeVendida: item.quantidadeCarrinho })
            });
        }

        alert("Venda realizada com sucesso!");

        // 3. O Pulo do Gato: Limpar e Atualizar
        carrinho = [];
        document.getElementById('modalPDV').style.display = 'none';

        // Essas duas funções garantem que a tela de produtos mostre os novos números
        await carregarProdutos();
        mostrarProdutos(); // Volta para a tela principal para o vendedor ver o resultado

    } catch (error) {
        console.error("Erro na venda:", error);
        alert("Houve um erro ao processar a venda.");
    }
}