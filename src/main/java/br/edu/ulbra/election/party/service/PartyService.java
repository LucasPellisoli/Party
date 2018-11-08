package br.edu.ulbra.election.party.service;

import br.edu.ulbra.election.party.exception.GenericOutputException;
import br.edu.ulbra.election.party.input.v1.PartyInput;
import br.edu.ulbra.election.party.model.Party;
import br.edu.ulbra.election.party.output.v1.GenericOutput;
import br.edu.ulbra.election.party.output.v1.PartyOutput;
import br.edu.ulbra.election.party.repository.PartyRepository;
import org.apache.commons.lang.StringUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.List;

@Service
public class PartyService {

    private final PartyRepository partyRepository;
    private final ModelMapper modelMapper;


    private static final String MESSAGE_INVALID_ID = "Invalid id";
    private static final String MESSAGE_PARTY_NOT_FOUND = "Party not found";

    @Autowired
    public PartyService(PartyRepository partyRepository, ModelMapper modelMapper) {
        this.partyRepository = partyRepository;
        this.modelMapper = modelMapper;
    }

    public List<PartyOutput> getAll(){
        Type partyOutputListType = new TypeToken<List<PartyOutput>>(){}.getType();
        return modelMapper.map(partyRepository.findAll(),partyOutputListType);
    }

    public PartyOutput create(PartyInput partyInput){
        this.validateInput(partyInput);
        this.valideteNewParty(partyInput)   ;
        Party party = modelMapper.map(partyInput, Party.class);
        party = partyRepository.save(party);
        return modelMapper.map(party, PartyOutput.class);
    }

    public PartyOutput getById(Long partyId){
        if (partyId == null){
            throw new GenericOutputException(MESSAGE_INVALID_ID);
        }
        Party party = partyRepository.findById(partyId).orElse(null);
        if (party == null){
            throw new GenericOutputException(MESSAGE_PARTY_NOT_FOUND);
        }
        return modelMapper.map(party, PartyOutput.class);
    }

    public PartyOutput update(Long partyId, PartyInput partyInput){
        if (partyId == null){
            throw new GenericOutputException(MESSAGE_INVALID_ID);
        }
        this.validateInput(partyInput);
        Party party = partyRepository.findById(partyId).orElse(null);
        if (party == null){
            throw new GenericOutputException(MESSAGE_PARTY_NOT_FOUND);
        }

        party.setCode(partyInput.getCode());
        party.setName(partyInput.getName());
        party.setNumber(partyInput.getNumber());

        party = partyRepository.save(party);
        return modelMapper.map(party, PartyOutput.class);
    }

    public GenericOutput delete(Long partyId){
        if (partyId == null){
            throw new GenericOutputException(MESSAGE_INVALID_ID);
        }
        Party party = partyRepository.findById(partyId).orElse(null);
        if (party == null){
            throw new GenericOutputException(MESSAGE_PARTY_NOT_FOUND);
        }
        partyRepository.delete(party);
        return new GenericOutput("Party deleted");
    }

    private void validateInput(PartyInput partyInput){
        if(StringUtils.isBlank(partyInput.getCode())){
            throw new GenericOutputException("Code isRequired!");
        }
        if(StringUtils.isBlank(partyInput.getName())){
            throw new GenericOutputException("Name isRequired!");
        }
        if(partyInput.getNumber() == null){
            throw new GenericOutputException("Number isRequired!");
        }
    }

    private void valideteNewParty(PartyInput partyInput){
        List<PartyOutput> partyOutputList  = this.getAll();

        for(PartyOutput partyOutput : partyOutputList){
            if(partyOutput.getCode().equals(partyInput.getCode())){
                throw new GenericOutputException("Party code is unavailable\n");
            }
            if(partyOutput.getNumber() == partyInput.getNumber()){
                throw new GenericOutputException("Party number is unavailable\n");
            }
        }
        
        if(String.valueOf(partyInput.getNumber()).length() != 2){
            throw new GenericOutputException("The number of a party must be composed of 2 digits\n");
        }
        if(partyInput.getName().length() < 5 ){
            throw new GenericOutputException("Name must be at least 5 letters\n");
        }
    }
}
