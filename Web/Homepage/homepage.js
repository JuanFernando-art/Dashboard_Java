let toggleButton = document.getElementById("toggle-menu");
let sidebar = document.getElementById("sidebar");
let dashboard = document.querySelector(".dashboard")

function loadUserFromRegister() {
  const params = new URLSearchParams(window.location.search);
  const idUsuarioUrl = params.get("idUsuario");

  if (idUsuarioUrl) {
    localStorage.setItem("idUsuario", idUsuarioUrl);
  }

  const idUsuario = localStorage.getItem("idUsuario");
  const nomeUsuario = localStorage.getItem("nomeUsuario");
  const userLabel = document.getElementById("homepage-user");

  if (userLabel && idUsuario) {
    userLabel.innerText = nomeUsuario ? `${nomeUsuario} (#${idUsuario})` : `ID #${idUsuario}`;
  }
}

loadUserFromRegister();

const logoutButton = document.getElementById("logout-button");
if (logoutButton) {
  logoutButton.addEventListener("click", () => {
    localStorage.removeItem("idUsuario");
    localStorage.removeItem("nomeUsuario");
    localStorage.removeItem("idEmpreendimento");
    window.location.href = "/index.html";
  });
}

function openEnterpriseWorkspace(idEmpreendimento) {
  if (!idEmpreendimento) return;

  localStorage.setItem("idEmpreendimento", idEmpreendimento);
  window.location.href = `../Enterprise/enterprise.html?idEmpreendimento=${idEmpreendimento}`;
}

function formatCurrency(value) {
  return `R$ ${Number(value || 0).toFixed(2)}`;
}

if (toggleButton) {
  toggleButton.addEventListener("click", () => {
    sidebar.classList.toggle("sidebar--open");
  });
}

const ventureModal = document.getElementById("venture-modal");
const openVentureModalButton = document.getElementById("open-venture-modal");
const closeVentureModalButton = document.getElementById("close-venture-modal");
const ventureForm = document.getElementById("venture-form");
const ventureError = document.getElementById("venture-error");
const ventureCepInput = document.getElementById("venture-cep");

function onlyDigits(value) {
  return value.replace(/\D/g, "");
}

function formatCep(value) {
  const digits = onlyDigits(value).slice(0, 8);
  if (digits.length <= 5) return digits;
  return `${digits.slice(0, 5)}-${digits.slice(5)}`;
}

async function fillAddressByCep() {
  const cep = onlyDigits(ventureCepInput.value);

  if (cep.length === 0) {
    ventureError.innerText = "";
    return;
  }

  if (cep.length !== 8) {
    ventureError.innerText = "Informe um CEP com 8 digitos.";
    return;
  }

  ventureError.innerText = "Buscando endereco...";

  try {
    const response = await fetch(`https://viacep.com.br/ws/${cep}/json/`);

    if (!response.ok) {
      throw new Error("Nao foi possivel consultar o CEP.");
    }

    const address = await response.json();

    if (address.erro) {
      throw new Error("CEP nao encontrado.");
    }

    document.getElementById("venture-street").value = address.logradouro || "";
    document.getElementById("venture-neighborhood").value = address.bairro || "";
    document.getElementById("venture-city").value = address.localidade || "";
    document.getElementById("venture-state").value = address.uf || "";
    ventureError.innerText = "";
  } catch (error) {
    ventureError.innerText = error.message;
  }
}

ventureCepInput.addEventListener("input", () => {
  ventureCepInput.value = formatCep(ventureCepInput.value);
});

ventureCepInput.addEventListener("blur", fillAddressByCep);

function openVentureModal() {
  ventureError.innerText = "";
  ventureModal.classList.add("modal--open");
}

function closeVentureModal() {
  ventureModal.classList.remove("modal--open");
  ventureForm.reset();
}

openVentureModalButton.addEventListener("click", openVentureModal);
closeVentureModalButton.addEventListener("click", closeVentureModal);

ventureModal.addEventListener("click", (event) => {
  if (event.target === ventureModal) {
    closeVentureModal();
  }
});

ventureForm.addEventListener("submit", async (event) => {
  event.preventDefault();

  const idUsuario = localStorage.getItem("idUsuario");
  const submitButton = ventureForm.querySelector("button[type='submit']");

  if (!idUsuario) {
    ventureError.innerText = "Cadastre um usuario antes de criar empreendimentos.";
    return;
  }

  const empreendimento = {
    nome: document.getElementById("venture-name").value.trim(),
    cnpj: document.getElementById("venture-cnpj").value.trim(),
    idUsuario: Number(idUsuario),
    endereco: {
      cep: document.getElementById("venture-cep").value.trim(),
      estado: document.getElementById("venture-state").value.trim(),
      cidade: document.getElementById("venture-city").value.trim(),
      bairro: document.getElementById("venture-neighborhood").value.trim(),
      logradouro: document.getElementById("venture-street").value.trim(),
      numero: Number(document.getElementById("venture-number").value)
    }
  };

  ventureError.innerText = "";
  submitButton.disabled = true;
  submitButton.innerText = "Cadastrando...";

  try {
    const response = await fetch("http://localhost:7000/api/empreendimentos", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(empreendimento)
    });

    const contentType = response.headers.get("content-type") || "";
    const data = contentType.includes("application/json")
      ? await response.json()
      : { mensagem: await response.text() };

    if (!response.ok) {
      throw new Error(data.erro || "Nao foi possivel cadastrar o empreendimento.");
    }

    closeVentureModal();
    await listResumeVentures();
    await loadPurchase();
  } catch (error) {
    ventureError.innerText = error.message;
  } finally {
    submitButton.disabled = false;
    submitButton.innerText = "Cadastrar empreendimento";
  }
});

async function loadPurchase() {
  try {
    const idUsuario = localStorage.getItem("idUsuario");
    const query = idUsuario ? `?idUsuario=${idUsuario}` : "";
    const resposta = await fetch (`http://localhost:7000/api/dashboard/total${query}`)
    const data = await resposta.json();
    
    console.log(data);

    const cardSales = document.getElementById("card--text-sales") 
    const cardExpenses = document.getElementById("card--text-expenses") 
    const cardProfit = document.getElementById("card--text-profit") 
  
      cardSales.innerText = formatCurrency(data.totalBruto)
      cardExpenses.innerText = formatCurrency(data.totalGasto)
      cardProfit.innerText = formatCurrency(data.totalLiquido)
  } catch (e) {
    console.log(e)
  }
}

loadPurchase()

async function listResumeVentures() {
    const cardResume = document.getElementById("card--resume-text")

    try {
      document.getElementById("dashboard__preview").innerHTML = "";
      document.getElementById("sidebar__ventures").innerHTML = "";
      const idUsuario = localStorage.getItem("idUsuario");
      const query = idUsuario ? `?idUsuario=${idUsuario}` : "";

      const response = await fetch(`http://localhost:7000/api/dashboard/empreendimentos${query}`);
  
      if (!response.ok) {
        throw new Error("Erro na requisição");
      }
  
      const data = await response.json();
  
      if (!data || data.length === 0) {
        console.log("Nenhum dado encontrado");
        return;
      }

      console.log(data)
  
      data.map((d) => {
        const card = document.createElement("div")
        const cardText = document.createElement("p")
        const metrics = document.createElement("div")
        const dashboard = document.getElementById("dashboard__preview")

        card.setAttribute("class", "card")
        card.setAttribute("role", "button")
        card.setAttribute("tabindex", "0")
        card.addEventListener("click", () => openEnterpriseWorkspace(d.idEmpreendimento))
        card.addEventListener("keydown", (event) => {
          if (event.key === "Enter" || event.key === " ") {
            event.preventDefault()
            openEnterpriseWorkspace(d.idEmpreendimento)
          }
        })
        cardText.setAttribute("id", "card--resume-text")
        cardText.innerText = d.nomeEmpreendimento
        card.appendChild(cardText)
        metrics.setAttribute("class", "enterprise-metrics")
        metrics.innerHTML = `
          <span><strong>Lucro</strong>${formatCurrency(d.lucroBruto)}</span>
          <span><strong>Gastos</strong>${formatCurrency(d.gastoBruto)}</span>
          <span><strong>Total liquido</strong>${formatCurrency(d.lucroLiquido)}</span>
        `
        card.appendChild(metrics)
        dashboard.appendChild(card)

        // sidebar

        const cardVenture = document.createElement("div")
        const cardVentureName = document.createElement("p")
        const sidebar = document.getElementById("sidebar__ventures")

        cardVentureName.innerText = d.nomeEmpreendimento
        cardVenture.setAttribute("class", "venture-card")
        cardVenture.setAttribute("role", "button")
        cardVenture.setAttribute("tabindex", "0")
        cardVenture.addEventListener("click", () => openEnterpriseWorkspace(d.idEmpreendimento))
        cardVenture.addEventListener("keydown", (event) => {
          if (event.key === "Enter" || event.key === " ") {
            event.preventDefault()
            openEnterpriseWorkspace(d.idEmpreendimento)
          }
        })
        cardVenture.appendChild(cardVentureName)
        sidebar.appendChild(cardVenture)

        const editButton = document.createElement("button")
        editButton.innerText = "Editar"
        editButton.setAttribute("class", "button-edit")
        editButton.addEventListener("click", (event) => event.stopPropagation())

        const deleteButton = document.createElement("button")
        deleteButton.innerText = "Excluir"
        deleteButton.setAttribute("class", "button-delete")
        deleteButton.addEventListener("click", (event) => event.stopPropagation())

        const fragment = document.createElement("div")
        fragment.setAttribute("class", "div-actions")

        fragment.appendChild(editButton)
        fragment.appendChild(deleteButton)

        cardVenture.appendChild(fragment)
      })
      
  
    } catch (error) {
      console.error("Erro:", error.message);
    }
  }

listResumeVentures()
