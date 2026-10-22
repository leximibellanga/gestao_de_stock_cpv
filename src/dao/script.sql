/* criar base de dados */
CREATE DATABASE IF NOT EXISTS cpv_gestao_stock CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

/* abrir base de dados */
USE cpv_gestao_stock;

/* criar tabela de produtos */
CREATE TABLE produtos (
  id INT AUTO_INCREMENT PRIMARY KEY,
  codigo VARCHAR(20) NOT NULL UNIQUE,
  nome VARCHAR(100) NOT NULL,
  categoria VARCHAR(50),
  preco DECIMAL(10,2) NOT NULL CHECK (preco >= 0),
  quantidade INT NOT NULL DEFAULT 0 CHECK (quantidade >= 0),
  stock_minimo INT NOT NULL DEFAULT 5,
  estado ENUM('ACTIVO','INACTIVO','ESGOTADO') NOT NULL DEFAULT 'ACTIVO',
  data_registo TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

/* criar tabela de movimentos(in/out) */
CREATE TABLE movimentos (
  id INT AUTO_INCREMENT PRIMARY KEY,
  produto_id INT NOT NULL,
  tipo ENUM('ENTRADA','SAIDA') NOT NULL,
  quantidade INT NOT NULL CHECK (quantidade > 0),
  observacao VARCHAR(200),
  data_movimento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (produto_id) REFERENCES produtos(id) ON DELETE CASCADE
);

/* inserir dados de teste na base de dados */
INSERT INTO produtos (codigo, nome, categoria, preco, quantidade, stock_minimo) VALUES
('P001','Arroz 5kg','Alimentar',350.00,40,10),
('P002','Óleo 2L','Alimentar',180.00,3,10),
('P003','Sabão em barra','Limpeza',45.00,25,8);
