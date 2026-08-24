package com.contractdrift.application;

import java.nio.file.Path;
import java.util.List;

import com.contractdrift.domain.Change;
import com.contractdrift.domain.Contract;
import com.contractdrift.domain.DiffEngine;
import com.contractdrift.domain.ContractParser;

/**
 * Application use case that orchestrates contract comparison.
 *
 * <p>
 * Follows hexagonal architecture: depends on the {@link ContractParser} port
 * (not a concrete adapter) and on the {@link DiffEngine} domain service.
 * The use case itself contains no business logic — it simply wires the
 * parser and the engine together.
 */
public class CompareContractsUseCase {

    private final ContractParser parser;
    private final DiffEngine engine = new DiffEngine();

    /**
     * @param parser port used to read OpenAPI files into domain objects
     */
    public CompareContractsUseCase(ContractParser parser) {
        this.parser = parser;
    }

    /**
     * Compares two OpenAPI files and returns all detected changes.
     *
     * @param oldContractPath path to the previous contract
     * @param newContractPath path to the new contract
     * @return list of changes found
     */
    public List<Change> execute(Path oldContractPath, Path newContractPath) {
        Contract oldContract = parser.parse(oldContractPath);
        Contract newContract = parser.parse(newContractPath);
        return engine.diff(oldContract, newContract);
    }

    /**
     * Compares two pre-parsed contracts and returns all detected changes.
     *
     * @param oldContract previous contract
     * @param newContract new contract
     * @return list of changes found
     */
    public List<Change> execute(Contract oldContract, Contract newContract) {
        return engine.diff(oldContract, newContract);
    }
}
