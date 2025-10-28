package com.laithevolution.annotationlab.service;

import com.laithevolution.annotationlab.model.Client;
import com.laithevolution.annotationlab.reposotory.ClientRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    @Transactional
    public Client createClient(Client client) {
        return clientRepository.save(client);
    }

    @Transactional
    public void deleteClient(Long id) {
        clientRepository.deleteById(id);
    }

    public Optional<Client> getClient(Long id) {
        return clientRepository.findById(id);
    }

    public Client findById(Long clientId) {
        return getClient(clientId).get();
    }

    public void updateClient(Client client) {
         clientRepository.save(client);
    }

    public List<Client> findByNameLike(String clientName) {
        return clientRepository.findByNameContaining(clientName);
    }
    public List<Client> findAll() {
        return clientRepository.findAll();
    }
}

