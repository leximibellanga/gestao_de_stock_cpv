# GUIA DE ADMINISTRAÇÃO PRÁTICA
## Sistema de Gestão de Stock

---

## 1. Requisitos

| Componente | Requisito |
|---|---|
| Sistema operativo | Windows 7 ou superior |
| Java | JRE/JDK 8 ou superior |
| Base de dados | MySQL (incluído no XAMPP) |
| Memória | 4 GB de RAM recomendados |
| Ecrã | Resolução mínima 1024x768 |

## 2. Instalação

### 2.1 Instalar o XAMPP
1. Descarregar o XAMPP em https://www.apachefriends.org/
2. Executar o instalador e manter os componentes **Apache**, **MySQL** e **phpMyAdmin**.
3. Abrir o **XAMPP Control Panel** e clicar em **Start** em *Apache* e *MySQL*.
4. Confirmar que os dois ficam com fundo verde.

### 2.2 Verificar o Java
Abrir a linha de comandos (`Win + R`, escrever `cmd`) e executar:

```
java -version
```

Deve aparecer a versão instalada. Se der erro, instalar o JDK/JRE.

### 2.3 Criar a base de dados
1. Abrir `http://localhost/phpmyadmin` no navegador.
2. Clicar no separador **SQL**.
3. Colar o conteúdo do ficheiro `gestao_stock.sql` e clicar em **Executar**.
4. Confirmar que a base `gestao_stock` aparece à esquerda, com as tabelas `produtos` e `movimentos`.

## 3. Execução da aplicação

### 3.1 A partir do NetBeans (desenvolvimento)
1. `File > Open Project` e seleccionar a pasta do projecto.
2. Botão direito em `view/Main.java` > **Run File** (`Shift + F6`).

### 3.2 Gerar o ficheiro executável (.jar)
1. No NetBeans: botão direito no projecto > **Properties > Run** e, em *Main Class*, escolher `view.Main`.
2. Botão direito no projecto > **Clean and Build**.
3. Na pasta do projecto, aparece a pasta `dist/` com:

```
dist/
├── GestaoStock.jar
└── lib/
    └── mysql-connector-j-x.x.x.jar
```

**Importante:** o `.jar` e a pasta `lib` têm de ficar sempre **juntos**. Para copiar para outro PC, copiar a pasta `dist` inteira.

### 3.3 Executar o .jar
- Duplo clique em `GestaoStock.jar`, ou
- Pela linha de comandos, dentro da pasta `dist`:

```
java -jar GestaoStock.jar
```

### 3.4 Atalho de arranque (opcional)
Criar um ficheiro `iniciar.bat` na pasta `dist` com o conteúdo:

```
@echo off
java -jar GestaoStock.jar
pause
```

Antes de abrir a aplicação, o **MySQL tem de estar iniciado** no XAMPP.

## 4. Manual de utilização

### 4.1 Dashboard
É a tela inicial. Mostra:
- **Total de produtos**, **unidades em stock** e **valor do stock**
- **Abaixo do mínimo** (vermelho) e **Esgotados** (laranja)
- Gráfico de unidades por categoria
- Tabela dos produtos críticos e dos últimos movimentos
- Resumo de entradas e saídas dos últimos 7 dias

### 4.2 Gerir produtos (aba *Produtos*)

| Operação | Como fazer |
|---|---|
| **Registar** | Preencher o formulário (código, nome, categoria, preço, quantidade inicial, stock mínimo) e clicar em **Guardar** |
| **Consultar** | A tabela lista todos os produtos; os que estão em alerta aparecem a vermelho |
| **Pesquisar** | Escrever código, nome ou categoria e clicar em **Pesquisar** (ou premir Enter) |
| **Actualizar** | Clicar numa linha, alterar os dados e clicar em **Guardar** |
| **Eliminar** | Clicar numa linha, clicar em **Eliminar** e confirmar |
| **Ver em alerta** | Clicar em **Abaixo do mínimo** |
| **Limpar formulário** | Clicar em **Novo / Limpar** |

Notas:
- O **código** é único e é convertido para maiúsculas.
- A **quantidade** só pode ser definida ao registar. Depois, só muda por entradas e saídas.
- Eliminar um produto apaga também o seu histórico de movimentos.
- Para retirar um produto de circulação sem o apagar, mudar o estado para **INACTIVO**.

### 4.3 Entradas e saídas (aba *Movimentos*)
1. Escolher o **produto** (a lista mostra o stock actual).
2. Escolher o **tipo**: ENTRADA (compra, reposição) ou SAIDA (venda, consumo).
3. Indicar a **quantidade** (número inteiro maior que zero).
4. Opcionalmente, escrever uma **observação** (ex.: "Compra ao fornecedor X").
5. Clicar em **Registar movimento**.

A aplicação avisa se o produto ficou **esgotado** ou **abaixo do mínimo**. O histórico, com cores por tipo, fica por baixo e pode ser filtrado pelo produto seleccionado.

### 4.4 Alertas (aba *Alertas de stock*)
Lista os produtos com `quantidade < stock mínimo` e a quantidade em falta. É a lista de reposição.

### 4.5 Estados dos produtos

| Estado | Significado | Como se define |
|---|---|---|
| ACTIVO | Em circulação, com stock | Automático |
| ESGOTADO | Quantidade igual a zero | Automático |
| INACTIVO | Fora de circulação; não pode ser movimentado | Manual (aba Produtos) |

## 5. Administração da base de dados

### 5.1 Cópia de segurança (backup)

**Pelo phpMyAdmin:**
1. Seleccionar a base `gestao_stock`.
2. Separador **Exportar** > método **Rápido** > formato **SQL** > **Executar**.
3. Guardar o ficheiro `.sql` num local seguro.

**Pela linha de comandos:**

```
C:\xampp\mysql\bin\mysqldump.exe -u root gestao_stock > backup_stock.sql
```

Recomendação: fazer um backup antes de eliminar dados em massa e guardar uma cópia fora do PC (pen drive ou nuvem).

### 5.2 Restauro
1. Garantir que existe a base `gestao_stock` (criar vazia, se necessário).
2. phpMyAdmin > seleccionar a base > separador **Importar** > escolher o ficheiro `.sql` > **Executar**.

Ou pela linha de comandos:

```
C:\xampp\mysql\bin\mysql.exe -u root gestao_stock < backup_stock.sql
```

### 5.3 Limpar os dados de teste
No separador **SQL** do phpMyAdmin, com a base `gestao_stock` seleccionada:

```sql
DELETE FROM movimentos;
DELETE FROM produtos;
ALTER TABLE movimentos AUTO_INCREMENT = 1;
ALTER TABLE produtos AUTO_INCREMENT = 1;
```

**Atenção:** esta operação apaga tudo e não pode ser desfeita. Fazer backup antes.

### 5.4 Segurança: utilizador dedicado
Por defeito o XAMPP tem o `root` sem palavra-passe, o que só é aceitável em ambiente de testes. Para uso real, criar um utilizador com permissões limitadas:

```sql
CREATE USER 'stock_app'@'localhost' IDENTIFIED BY 'ESCOLHER_UMA_SENHA_FORTE';
GRANT SELECT, INSERT, UPDATE, DELETE ON gestao_stock.* TO 'stock_app'@'localhost';
FLUSH PRIVILEGES;
```

Depois, em `util/Conexao.java`, alterar:

```java
private static final String USER = "stock_app";
private static final String PASSWORD = "ESCOLHER_UMA_SENHA_FORTE";
```

E refazer **Clean and Build**.

## 6. Resolução de problemas

| Mensagem / sintoma | Causa provável | Solução |
|---|---|---|
| `Communications link failure` | MySQL desligado | Iniciar o MySQL no XAMPP |
| `Unknown database 'gestao_stock'` | Base não criada | Executar o script SQL (secção 2.3) |
| `Access denied for user` | Utilizador ou senha errados | Ajustar `USER` e `PASSWORD` em `Conexao.java` |
| `Driver MySQL não encontrado` | Biblioteca em falta | NetBeans: adicionar *MySQL JDBC Driver*; no `.jar`, manter a pasta `lib` ao lado |
| O `.jar` não abre com duplo clique | Java não associado ou *Main Class* não definida | Executar `java -jar GestaoStock.jar` e ler o erro; definir `view.Main` |
| MySQL não arranca no XAMPP | Porta 3306 ocupada | Fechar outro MySQL ou alterar a porta no XAMPP e no `URL` de `Conexao.java` |
| "Já existe um produto com o código..." | Código duplicado | Usar outro código |
| "Stock insuficiente" | Saída maior que o stock | Reduzir a quantidade ou registar antes uma entrada |
| Produto não aparece em *Movimentos* | Estado INACTIVO | Mudar o estado para ACTIVO na aba *Produtos* |
| Dados não actualizam | Painel desactualizado | Mudar de aba e voltar |

## 7. Boas práticas de manutenção

- Iniciar o **MySQL** antes da aplicação e fechá-lo no fim do dia.
- Fazer **backup** com regularidade (diário ou semanal, conforme o uso).
- Registar **observações** nos movimentos para facilitar auditorias.
- Rever a aba **Alertas** no início de cada dia.
- Definir um **stock mínimo** realista para cada produto.
- Não editar a base de dados directamente no phpMyAdmin durante o uso da aplicação.