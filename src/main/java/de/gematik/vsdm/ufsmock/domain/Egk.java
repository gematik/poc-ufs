/*
 * Copyright 2024 gematik GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.vsdm.ufsmock.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Egk {


  @Id
  private String iccsn;
  @NotNull
  @Column(unique = true)
  private String kvnr;


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Egk egk)) {
      return false;
    }
    return Objects.equals(iccsn, egk.iccsn) && Objects.equals(kvnr, egk.kvnr);
  }

  @Override
  public int hashCode() {
    return Objects.hash(iccsn, kvnr);
  }

  @Override
  public String toString() {
    return "Egk{" +
        "iccsn='" + iccsn + '\'' +
        ", kvnr='" + kvnr + '\'' +
        '}';
  }
}



