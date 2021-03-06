package io.aif.language.word.dict;

import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.aif.language.common.IDict;
import io.aif.language.common.IDictBuilder;
import io.aif.language.common.IGrouper;
import io.aif.language.common.IMapper;
import io.aif.language.token.comparator.ITokenComparator;
import io.aif.language.word.IWord;
import io.aif.language.word.comparator.IGroupComparator;

public class DictBuilder implements IDictBuilder<Collection<String>, IWord> {

  private static final Logger logger = Logger.getLogger(DictBuilder.class);

  private final IGrouper grouper;

  private final IMapper<WordMapper.DataForMapping, IWord> groupToWordMapper;

  public DictBuilder() {
    final ITokenComparator tokenComparator = ITokenComparator.defaultComparator();
    final IGroupComparator groupComparator =
        IGroupComparator.createDefaultInstance(tokenComparator);

    this.groupToWordMapper = new WordMapper(new RootTokenExtractor(tokenComparator));
    this.grouper = new FormGrouper(groupComparator);
  }

  public DictBuilder(final IGrouper grouper,
                     final IMapper<WordMapper.DataForMapping, IWord> groupToWordMapper) {
    this.grouper = grouper;
    this.groupToWordMapper = groupToWordMapper;
  }

  @Override
  public IDict<IWord> build(final Collection<String> from) {
    logger.debug(String.format("Beginning to build a dict: %s", from));

    List<Set<String>> groups = grouper.group(from);
    logger.debug(String.format("Tokens after grouping: %s", groups));

    List<WordMapper.DataForMapping> dataForMapping = groups.parallelStream().map(group -> {
      final Long count = from.stream().filter(group::contains).count();
      return new WordMapper.DataForMapping(group, count);
    }).collect(Collectors.toList());

    List<IWord> iWords = groupToWordMapper.mapAll(dataForMapping);
    logger.debug(String.format("IWords created: %s", iWords));

    IDict dict = Dict.create(new HashSet<>(iWords));
    logger.debug(String.format("Dict generated: %s", dict.getWords()));
    return dict;
  }
}
