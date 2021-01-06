package com.merlobranco.springboot.app.models.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.merlobranco.springboot.app.models.entity.Cliente;

public interface ClienteDao extends PagingAndSortingRepository<Cliente, Long>{

	// Should be left outer join in order to retrieve the clients who don't have facturas
	@Query("select c from Cliente c left join fetch c.facturas f where c.id= ?1 ")
	public Cliente fecthByIdWithFacturas(Long id); 
}
