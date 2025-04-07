CREATE TABLE IF NOT EXISTS products (
  id SERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  description TEXT,
  price NUMERIC NOT NULL
);

INSERT INTO products (name, description, price) VALUES
('Produto 1', 'Primeiro produto', 100.50),
('Produto 2', 'Segundo produto', 199.99);
