package com.faculdade.bikepark.web.rest;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.faculdade.bikepark.domain.Localizacao;
import com.faculdade.bikepark.repository.LocalizacaoRepository;
import com.faculdade.bikepark.repository.search.LocalizacaoSearchRepository;
import com.faculdade.bikepark.web.rest.errors.BadRequestAlertException;
import com.faculdade.bikepark.web.rest.util.HeaderUtil;

import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing Localizacao.
 */
@RestController
@RequestMapping("/api")
public class LocalizacaoResource {

    private final Logger log = LoggerFactory.getLogger(LocalizacaoResource.class);

    private static final String ENTITY_NAME = "localizacao";

    private final LocalizacaoRepository localizacaoRepository;

    private final LocalizacaoSearchRepository localizacaoSearchRepository;

    public LocalizacaoResource(LocalizacaoRepository localizacaoRepository, LocalizacaoSearchRepository localizacaoSearchRepository) {
        this.localizacaoRepository = localizacaoRepository;
        this.localizacaoSearchRepository = localizacaoSearchRepository;
    }

    /**
     * POST  /localizacaos : Create a new localizacao.
     *
     * @param localizacao the localizacao to create
     * @return the ResponseEntity with status 201 (Created) and with body the new localizacao, or with status 400 (Bad Request) if the localizacao has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/localizacaos")
    @Timed
    public ResponseEntity<Localizacao> createLocalizacao(@Valid @RequestBody Localizacao localizacao) throws URISyntaxException {
        log.debug("REST request to save Localizacao : {}", localizacao);

        Localizacao result = localizacaoRepository.save(localizacao);
        localizacaoSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/localizacaos/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /localizacaos : Updates an existing localizacao.
     *
     * @param localizacao the localizacao to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated localizacao,
     * or with status 400 (Bad Request) if the localizacao is not valid,
     * or with status 500 (Internal Server Error) if the localizacao couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/localizacaos")
    @Timed
    public ResponseEntity<Localizacao> updateLocalizacao(@Valid @RequestBody Localizacao localizacao) throws URISyntaxException {
        log.debug("REST request to update Localizacao : {}", localizacao);
        if (localizacao.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        
         int foobar = localizacao.getQtdReservada() + 1;
         localizacao.setQtdReservada(foobar);
         
        Localizacao result = localizacaoRepository.save(localizacao);
        localizacaoSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, localizacao.getId().toString()))
            .body(result);
    }
    
    @PostMapping("/localizacaos/cancela")
    @Timed
    public Localizacao liberaLocalizacao(@RequestBody Localizacao localizacao) throws URISyntaxException {
        log.debug("REST request to save Localizacao : {}", localizacao);

        Localizacao loca = localizacaoRepository.findByEnderecoAndNumero(localizacao.getEndereco(), localizacao.getNumero());
        
        if(loca == null) {
        	throw new BadRequestAlertException("Localizacao não encontrada", "Localizacao não encontrada", "Localizacao não encontrada");
        } else {
        	int foobar = loca.getQtdReservada() - 1;
        	loca.setQtdReservada(foobar);
        }
        
        
        Localizacao result = localizacaoRepository.save(loca);
        
        localizacaoSearchRepository.save(result);
        return result;
    }

    /**
     * GET  /localizacaos : get all the localizacaos.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of localizacaos in body
     */
    @GetMapping("/localizacaos")
    @Timed
    public List<Localizacao> getAllLocalizacaos() {
        log.debug("REST request to get all Localizacaos");
        
        ArrayList<Localizacao> foobar = (ArrayList<Localizacao>) localizacaoRepository.findAll();
        ArrayList<Localizacao> returnOK = new ArrayList<Localizacao>();
        
        for (Localizacao localizacao : foobar) {
			if(localizacao.getQtdTotais() > localizacao.getQtdReservada()) {
				returnOK.add(localizacao);
			}
		}
        
        return returnOK;
    }

    /**
     * GET  /localizacaos/:id : get the "id" localizacao.
     *
     * @param id the id of the localizacao to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the localizacao, or with status 404 (Not Found)
     */
    @GetMapping("/localizacaos/{id}")
    @Timed
    public ResponseEntity<Localizacao> getLocalizacao(@PathVariable String id) {
        log.debug("REST request to get Localizacao : {}", id);
        Optional<Localizacao> localizacao = localizacaoRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(localizacao);
    }

    /**
     * DELETE  /localizacaos/:id : delete the "id" localizacao.
     *
     * @param id the id of the localizacao to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/localizacaos/{id}")
    @Timed
    public ResponseEntity<Void> deleteLocalizacao(@PathVariable String id) {
        log.debug("REST request to delete Localizacao : {}", id);

        localizacaoRepository.deleteById(id);
        localizacaoSearchRepository.deleteById(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id)).build();
    }

    /**
     * SEARCH  /_search/localizacaos?query=:query : search for the localizacao corresponding
     * to the query.
     *
     * @param query the query of the localizacao search
     * @return the result of the search
     */
    @GetMapping("/_search/localizacaos")
    @Timed
    public List<Localizacao> searchLocalizacaos(@RequestParam String query) {
        log.debug("REST request to search Localizacaos for query {}", query);
        return StreamSupport
            .stream(localizacaoSearchRepository.search(queryStringQuery(query)).spliterator(), false)
            .collect(Collectors.toList());
    }

}
