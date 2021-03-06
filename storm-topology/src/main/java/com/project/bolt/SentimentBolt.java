package com.project.bolt;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.model.twitter.CustomStatus;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

/**
 * Implementation of the sentiment Bolt that receives a tuple representing a CustomStatus, calculates the sentiment, adds the sentiment to the status and emits
 * the status. The sentiment is a number between -2 and 2. Negative numbers indicate a negative sentiment, 0 indicates a neutral sentiment and positive numbers
 * indicate a positive sentiment.
 * 
 * @author rdonnarumma
 * 
 */
public class SentimentBolt extends BaseBasicBolt {
    private static final long serialVersionUID = 3201910429837431413L;

    private static final Logger LOG = LoggerFactory.getLogger(SentimentBolt.class);

    private static StanfordCoreNLP pipeline = new StanfordCoreNLP("nlp/nlp.properties");

    private static Map<Integer, Integer> sentimentMap = new HashMap<>();

    private static ObjectMapper mapper = new ObjectMapper();

    static {
        sentimentMap.put(1, -2);
        sentimentMap.put(2, -1);
        sentimentMap.put(3, 0);
        sentimentMap.put(4, 1);
        sentimentMap.put(5, 2);
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        for (Object obj : input.getValues()) {
            if (obj instanceof CustomStatus) {
                CustomStatus status = (CustomStatus)obj;
                int mainSentiment = 0;
                if ((status.getText() != null) && (status.getText().length() > 0)) {
                    int longest = 0;
                    Annotation annotation = pipeline.process(status.getText());
                    for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                        Tree tree = sentence.get(SentimentCoreAnnotations.AnnotatedTree.class);
                        int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
                        String partText = sentence.toString();
                        if (partText.length() > longest) {
                            mainSentiment = sentiment;
                            longest = partText.length();
                        }
                    }
                }
                status.setSentiment(sentimentMap.get(mainSentiment));
                try {
                    collector.emit(new Values(mapper.writeValueAsString(status)));
                } catch (JsonProcessingException e) {
                    LOG.error(String.format("Error writing status with id: %s", status.getId()), e);
                }
            }
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("sentiment"));
    }

}
