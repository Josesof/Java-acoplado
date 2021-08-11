package com.bolsadeideas.springboot.web.app.models.dao;


import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.bolsadeideas.springboot.web.app.models.entity.Cliente;

//Cambiamos el crudrepository para utilizar paginacion esto es lo mismo
//public interface IClienteDao extends CrudRepository <Cliente, Long> 
public interface IClienteDao extends PagingAndSortingRepository<Cliente, Long> {


	@Query("select c from Cliente  c left join fetch c.facturas f where c.id=?1")
	public Cliente fetchByIdWithFacturas(Long id);
}
