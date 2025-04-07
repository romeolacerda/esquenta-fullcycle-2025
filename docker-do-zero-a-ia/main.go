package main

import (
	"database/sql"
	"encoding/json"
	"fmt"
	"log"
	"net/http"

	_"github.com/lib/pq"
)

type Product struct{
	ID	int	`json:"id"`
	Name string `json:"name"`
	Description string `json:"description"`
	Price float64 `json:"price"`
}

func main(){
	//config postgres
	connStr := "host=postgres port=5432 user=postgres password=postgres dbname=postgres sslmode=disable"
	db, err := sql.Open("postgres", connStr)
	if err != nil{
		log.Fatal(err)
	}
	defer db.Close()

	http.HandleFunc("/products", func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodGet{
			w.WriteHeader(http.StatusMethodNotAllowed)
			return
		}

		products, err := listProducts(db)
		if err != nil {
			w.WriteHeader(http.StatusInternalServerError)
			fmt.Fprint(w, "Error listing products", err)
			return
		}

		w.Header().Set("Content-Type", "application/json")
		json.NewEncoder(w).Encode(products)
	})
	
	fmt.Println("Server running on port 8080. . .")
	http.ListenAndServe(":8080", nil)
}

func listProducts(db *sql.DB) ([]Product, error){
	rows, err := db.Query("SELECT id, name, description, price FROM products")
	if err != nil{
		return nil, err
	}
	defer rows.Close()

	var products []Product
	for rows.Next() {
		var p Product
		err := rows.Scan(&p.ID, &p.Name, &p.Description, &p.Price)
		if err != nil {
			return nil, err
		}
		products = append(products, p)
	}
	return products, nil
}