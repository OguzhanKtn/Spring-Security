package com.works.services;

import com.works.configs.Rest;
import com.works.entities.Customer;
import com.works.entities.Role;
import com.works.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService implements UserDetailsService {

    final CustomerRepository repository;
    final PasswordEncoder passwordEncoder;

    public ResponseEntity register(Customer customer){
        String newPassword = passwordEncoder.encode(customer.getPassword());
        customer.setPassword(newPassword);
        if(repository.existsByEmailEqualsIgnoreCase(customer.getEmail())){
            Rest rest = new Rest(false,"This account is already exist !");
            return new ResponseEntity(rest, HttpStatus.BAD_REQUEST);
        }
        Rest rest = new Rest(true,repository.save(customer));
        return new ResponseEntity(rest, HttpStatus.OK);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Customer> optionalCustomer = repository.findByEmailEqualsIgnoreCase(username);
        if(optionalCustomer.isPresent()){
            Customer customer = optionalCustomer.get();
            return new User(customer.getEmail(), customer.getPassword(), parseRole(customer.getRoles()));
        }
            throw new UsernameNotFoundException("Not Found");
    }

    private Collection<? extends GrantedAuthority> parseRole(List<Role> roles) {
        List<GrantedAuthority> ls = new ArrayList<>();
        for(Role item : roles){
            ls.add(new SimpleGrantedAuthority(item.getName()));
        }
        return ls;
    }
}

