/**
 * Copyright 2020 Alexander Herzog
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ui.simplesimulation;

/**
 * Eingabeparameter für die Einfach-Simulation
 * @author Alexander Herzog
 * @version 1.0
 * @see SimpleSimulation
 */
public class SimpleSimulationInput {
	/** Ankunftsrate (in Kunden/Minute) */
	public final double lambda;

	/** Erwartungswert der Wartezeittoleranz (in Minuten) */
	public final double EWT;

	/** Standardabweichung der Wartezeittoleranz (in Minuten) */
	public final double StdWT;

	/** Wiederholwahrscheinlichkeit */
	public final double retryProbability;

	/** Erwartungswert der Wiederholabstände (in Minuten) */
	public final double ERetry;

	/** Anzahl an Agenten im System */
	public final int c;

	/** Erwartungswert der Bediendauern (in Minuten) */
	public final double ES;

	/** Standardabweichung der Bediendauern (in Minuten) */
	public final double StdS;

	/** Erwartungswert der Nachbearbeitungszeiten (in Minuten) */
	public final double ES2;

	/** Standardabweichung der Nachbearbeitungszeiten (in Minuten) */
	public final double StdS2;

	/** Weiterleitungswahrscheinlichkeit */
	public final double continueProbability;

	/**
	 * Konstruktor der Klasse <code>SimpleSimulationInput</code>
	 * @param lambda	Ankunftsrate (in Kunden/Minute)
	 * @param EWT	Erwartungswert der Wartezeittoleranz (in Minuten)
	 * @param StdWT	Standardabweichung der Wartezeittoleranz (in Minuten)
	 * @param retryProbability	Wiederholwahrscheinlichkeit
	 * @param ERetry	Erwartungswert der Wiederholabstände (in Minuten)
	 * @param c	Anzahl an Agenten im System
	 * @param ES	Erwartungswert der Bediendauern (in Minuten)
	 * @param StdS	Standardabweichung der Bediendauern (in Minuten)
	 * @param ES2	Erwartungswert der Nachbearbeitungszeiten (in Minuten)
	 * @param StdS2	Standardabweichung der Nachbearbeitungszeiten (in Minuten)
	 * @param continueProbability	Weiterleitungswahrscheinlichkeit
	 */
	public SimpleSimulationInput(final double lambda, final double EWT, final double StdWT, final double retryProbability, final double ERetry, final int c, final double ES, final double StdS, final double ES2, final double StdS2, final double continueProbability) {
		this.lambda=lambda;
		this.EWT=EWT;
		this.StdWT=StdWT;
		this.retryProbability=retryProbability;
		this.ERetry=ERetry;
		this.c=c;
		this.ES=ES;
		this.StdS=StdS;
		this.ES2=ES2;
		this.StdS2=StdS2;
		this.continueProbability=continueProbability;
	}

	/**
	 * Prüft, ob zwei <code>SimpleSimulationInput</code>-Objekte inhaltlich identisch sind.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SimpleSimulationInput) {
			SimpleSimulationInput input=(SimpleSimulationInput)obj;
			if (lambda!=input.lambda) return false;
			if (EWT!=input.EWT) return false;
			if (StdWT!=input.StdWT) return false;
			if (retryProbability!=input.retryProbability) return false;
			if (ERetry!=input.ERetry) return false;
			if (c!=input.c) return false;
			if (ES!=input.ES) return false;
			if (StdS!=input.StdS) return false;
			if (ES2!=input.ES2) return false;
			if (StdS2!=input.StdS2) return false;
			if (continueProbability!=input.continueProbability) return false;
			return true;
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		assert false : "hashCode not designed";
	return 0;
	}
}