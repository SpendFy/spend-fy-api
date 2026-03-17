package br.com.ufape.spendfy.service;

import br.com.ufape.spendfy.dto.auth.UserCreateDTO;
import br.com.ufape.spendfy.dto.auth.UserResponseDTO;
import br.com.ufape.spendfy.entity.User;
import br.com.ufape.spendfy.exception.DuplicateResourceException;
import br.com.ufape.spendfy.exception.ResourceNotFoundException;
import br.com.ufape.spendfy.mapper.EntityMapper;
import br.com.ufape.spendfy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final EntityMapper mapper;
    private final PasswordEncoder passwordEncoder;
    
    public UserResponseDTO createUser(UserCreateDTO createDTO) {
        if (userRepository.existsByEmail(createDTO.getEmail())) {
            throw new DuplicateResourceException("User with email already exists: " + createDTO.getEmail());
        }
        
        User user = User.builder()
            .name(createDTO.getName())
            .email(createDTO.getEmail())
            .password(passwordEncoder.encode(createDTO.getPassword()))
            .active(true)
            .build();
        
        User savedUser = userRepository.save(user);
        return mapper.toUserResponseDTO(savedUser);
    }
    
    public UserResponseDTO getUserById(String id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return mapper.toUserResponseDTO(user);
    }
    
    public UserResponseDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return mapper.toUserResponseDTO(user);
    }
    
    public User findUserById(String id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }
    
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
    
    public void deleteUser(String id) {
        User user = findUserById(id);
        user.setActive(false);
        userRepository.save(user);
    }
}