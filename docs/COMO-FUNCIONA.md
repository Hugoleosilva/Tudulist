# Tudulist: como funciona

Guia de uso e de funcionamento do app. Para instalar, veja o [README](../README.md).

**Índice**
[Visão geral](#visão-geral) ·
[Cadastrar uma atividade](#cadastrar-uma-atividade) ·
[Tipos de repetição](#tipos-de-repetição) ·
[Ver e editar](#ver-editar-e-excluir) ·
[Mudar só um dia](#mudar-só-um-dia) ·
[Choque de horário](#choque-de-horário) ·
[Alertas](#alertas) ·
[Seus dados](#seus-dados) ·
[Perguntas frequentes](#perguntas-frequentes)

---

## Visão geral

O app tem quatro abas na parte de baixo e um botão **+** para cadastrar:

| Aba | O que mostra |
|---|---|
| **Hoje** | As atividades de hoje, em ordem de horário. |
| **Amanhã** | Os lembretes do dia seguinte, para você se preparar. |
| **Semana** | Os 7 dias da semana (domingo a sábado), uma linha por atividade. Setas trocam de semana. |
| **Mês** | O calendário do mês. Ao tocar num dia, a lista dele aparece embaixo. |

No topo ficam sempre a hora e a data. A cor da barrinha ao lado de cada atividade indica o tipo:
**laranja** = Treino, **azul** = Estudo, **roxo** = Outro.

O app abre direto na aba **Hoje**, sem login nem telas de boas-vindas.

### Atalhos que economizam toque

- O **+** já vem com a data certa: na aba **Amanhã** cadastra para amanhã, na aba **Mês** cadastra para o dia
  que você selecionou, nas outras cadastra para hoje.
- Na aba **Semana**, tocar numa linha **livre** (ou fora de uma atividade) abre o cadastro para aquele dia.
- Em **Semana** e **Mês**, tocar no título (a semana ou o mês no meio) volta para o período atual.

---

## Cadastrar uma atividade

Toque em **+**. Os campos são:

| Campo | Como funciona |
|---|---|
| **Atividade** | O nome. É o único campo obrigatório. |
| **Tipo** | Treino, Estudo ou Outro. Só muda a cor e a bolinha no calendário. |
| **Repetir** | Uma vez, Todo dia, Semanal ou Mensal (veja abaixo). |
| **Dias da semana** | Aparece só em **Semanal**. Toque nos dias que quiser. |
| **Início** e **Fim** | O horário da atividade. O fim já vem 1 hora depois do início e precisa ser depois dele. |
| **Data** / **A partir de** | Em "Uma vez" é o dia da atividade. Nos outros tipos é a data em que a repetição **começa**. |
| **Alerta antes do início** | Não, 15 min, 30 min, 1h ou 1h30. O padrão é 30 min. |
| **Anotações** | Opcional. Não aparecem na lista, só ao tocar na atividade. |

No fim do cadastro há um resumo, por exemplo *"Repete: Ter · Qui · alerta 30 min antes"*, para você conferir
antes de salvar.

> Se você mudar o **Início** para um horário depois do **Fim**, o fim se ajusta sozinho (1 hora depois).

### Exemplo: rotina de treino e estudo

1. **Muay Thai** · Treino · Semanal · Ter e Qui · 06:00 às 07:00 · alerta 1h.
2. **Inglês** · Estudo · Todo dia · 08:30 às 09:30 · alerta 15 min.
3. **Pilates** · Treino · Uma vez · sexta, 15:00 às 16:00. É um compromisso pontual, não repete.

Várias atividades no mesmo dia funcionam normalmente (treino de manhã, estudo à tarde...).

---

## Tipos de repetição

| Tipo | Quando acontece |
|---|---|
| **Uma vez** | Só na data escolhida. |
| **Todo dia** | Todos os dias, a partir da data escolhida. |
| **Semanal** | Nos dias da semana marcados, toda semana, a partir da data escolhida. |
| **Mensal** | No mesmo número de dia, todo mês. O dia vem da data escolhida. |

**Mensal em meses curtos:** se a data for dia 31 e o mês só tiver 30 dias (ou 28), a atividade acontece
no **último dia do mês**.

As repetições **não são gravadas dia a dia**: o app guarda a regra e calcula o que acontece em cada dia na
hora de mostrar. Por isso é possível ver meses à frente sem pesar.

---

## Ver, editar e excluir

- **Tocar numa atividade** abre os **detalhes**: horário, tipo, repetição e a **anotação**, se houver.
  Uma atividade com anotação mostra um 📝 ao lado do tipo.
- Nos detalhes, **Editar** abre o cadastro com tudo preenchido.
- **Excluir** fica dentro da tela de edição e pede confirmação. Em uma atividade que se repete, **apaga todas
  as repetições**. Se você só quer tirar um dia, use [Só este dia](#mudar-só-um-dia).

---

## Mudar só um dia

Em atividades que se repetem, o botão **Só este dia** (nos detalhes) permite abrir uma exceção sem mexer na
série:

- **Salvar** com um novo início e fim muda o horário **somente naquela data**.
- **Pular este dia** faz a atividade não acontecer naquela data.

Todas as outras datas continuam como estavam. Exemplo: o Inglês de sexta passa para 11:00 só nesta
sexta; nas outras sextas continua às 08:30.

As exceções ficam guardadas com a atividade. Se você **editar a série inteira** depois, as exceções são
mantidas.

---

## Choque de horário

Ao **salvar** uma atividade (nova ou editada), o app confere se ela se sobrepõe a outra nos **próximos 90
dias**. Se houver choque, aparece **"Choque de horário"** com o dia e três opções:

1. **Alterar só "(a atividade que você está salvando)" neste dia**
2. **Alterar só "(a outra atividade)" neste dia**
3. **Manter os dois assim**

Ao escolher 1 ou 2, abre a janela de [Só este dia](#mudar-só-um-dia), para mudar o horário daquele dia ou
pular. Depois o app confere de novo: se ainda houver choque, ou se existir o mesmo choque em outras datas,
ele pergunta de novo.

Detalhes que ajudam a entender:

- **O que conta como choque:** os intervalos se sobrepõem. Uma atividade que termina às 07:00 e outra que
  começa às 07:00 **não** é choque.
- **Sem fim definido,** o app considera 1 hora de duração.
- Se o mesmo choque acontece em várias datas (duas atividades semanais no mesmo horário), o aviso mostra
  em quantos dias e você resolve **um dia por vez**. **Manter os dois assim** encerra e salva tudo.
- **Cancelar** volta para o cadastro aberto, sem perder o que você digitou.
- O app só verifica choques da atividade que você está salvando. Choques que já existiam entre outras duas
  atividades não são apontados.

---

## Alertas

Cada atividade tem o seu alerta (Não, 15 min, 30 min, 1h ou 1h30 antes do início). Com **Não**, nenhuma
notificação é criada para ela.

**Como funciona por baixo:**

1. O app procura qual é o **próximo lembrete** entre todas as atividades e agenda **um** alarme exato do
   Android para aquela hora.
2. Quando o alarme toca, o app mostra a notificação (por exemplo *"Muay Thai em 1h"*, com o horário e o tipo
   embaixo) e agenda o lembrete seguinte.
3. O app reagenda tudo sozinho quando você **liga o celular**, **atualiza o app** ou **muda a hora ou o fuso**,
   e sempre que você salva, edita ou exclui uma atividade.
4. Mudanças feitas em [Só este dia](#mudar-só-um-dia) também valem: o alerta usa o horário alterado.

**Para o alerta ser confiável no seu celular:**

- Permita as **notificações** quando o app pedir.
- Deixe a bateria do app em **Sem restrições** (Configurações > Apps > Tudulist > Bateria). Sem isso, alguns
  celulares atrasam ou cortam o aviso para economizar bateria.
- O modo **Não incomodar** pode silenciar o aviso.

**Limites que vale conhecer:**

- Se o celular estiver **desligado** na hora do lembrete, aquele aviso se perde: ele não aparece depois.
- Se você cadastra uma atividade **quando o horário do alerta já passou** (por exemplo, cria às 05:40 um treino
  das 06:00 com alerta de 30 min), não há alerta para aquele dia. Os próximos dias seguem normalmente.

---

## Seus dados

- As atividades ficam **só no seu celular**, no armazenamento privado do app. Nenhum dado é enviado a
  lugar nenhum: o app **não tem permissão de internet**.
- **Desinstalar o app, ou limpar os dados dele, apaga todas as atividades.**
- O app **não tem backup nem exportação próprios**. O Android pode incluir os dados do app no backup
  automático da sua conta Google, mas isso depende do celular e **não é garantido**. Não conte com isso para
  trocar de aparelho.
- O app só mostra o tema **escuro**.

---

## Perguntas frequentes

**O alerta não chegou. O que verifico?**
1. As notificações estão permitidas? (Configurações > Apps > Tudulist > Notificações.)
2. A bateria do app está em **Sem restrições**?
3. O alerta da atividade está em **Não**? Abra a atividade e confira.
4. O celular estava desligado, ou em **Não incomodar**, na hora?
5. A atividade foi criada depois do horário do alerta daquele dia?

**Como atualizo o app sem perder minhas atividades?**
Instale o novo `.apk` **por cima** do antigo, sem desinstalar. As atividades são mantidas. Se o Android
responder **"App não instalado"**, o arquivo novo foi assinado com uma chave diferente da do instalado
(acontece quando é gerado em outro computador). Nesse caso a única saída é desinstalar o antigo, e isso
apaga as atividades, então anote-as antes.

**Posso usar no iPhone?**
Não. O app é só para Android 8.0 ou mais novo.

**Posso ter o mesmo treino em dias e horários diferentes?**
Sim. Cadastre uma atividade para cada combinação (por exemplo, Muay Thai terça às 06:00 e outra para sábado
às 09:00), ou use [Só este dia](#mudar-só-um-dia) para mudar o horário de uma data específica.

**Apaguei uma atividade sem querer.**
Não há lixeira nem desfazer. Cadastre de novo.

**O que muda se eu mudar a hora ou o fuso do celular?**
O app usa a hora do aparelho e reagenda os alertas automaticamente.
