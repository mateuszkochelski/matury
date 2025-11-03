import { getFieldData } from "@/app/utils/getFieldData";
import { Breadcrumb } from "@/components/Breadcrumb";
import { FavoriteButton } from "@/components/FavoriteButton";
import CustomLineChart, {
  ThresholdGraphData,
} from "@/components/custom-line-chart/CustomLineChart";
import FieldsCarousel from "@/components/fields-carousel/FieldsCarousel";
import { Card, CardHeader, CardTitle, CardContent, CardDescription } from "@/components/ui/card";
import { Calculator } from "lucide-react";
import { notFound, redirect } from "next/navigation";

export default async function Page({
  params,
}: {
  params: Promise<{ universityId: string; departmentId: string; fieldId: string }>;
}) {
  const { universityId, departmentId, fieldId } = await params;
  if (!fieldId) {
    notFound();
  }

  const { fieldData, departmentFields, thresholdData } = await getFieldData(fieldId);

  const thresholds = thresholdData.content;

  // needed variables
  let maxPhase = 0;
  let maxYear = 0;
  let latestAdmissionLimit = 0;
  thresholds.forEach((threshold) => {
    if (threshold.phase > maxPhase) maxPhase = threshold.phase;
    if (threshold.year >= maxYear) {
      maxYear = threshold.year;
      // this works because we get thresholds sorted by years ascending from BE
      latestAdmissionLimit =
        threshold.admissionLimit && threshold.phase === 1
          ? threshold.admissionLimit
          : latestAdmissionLimit;
    }
  });

  // Grouping thresholds by phases (for the graphs)
  const thresholdsInPhases: ThresholdGraphData[][] = [];
  for (let i = 1; i <= maxPhase; i++) {
    thresholdsInPhases.push([]);
  }
  thresholds.forEach((threshold) => {
    thresholdsInPhases[threshold.phase - 1].push({
      year: threshold.year,
      threshold: threshold.threshold,
      admissionRate:
        threshold.admissionLimit !== null && threshold.admissions !== null
          ? Math.round((threshold.admissionLimit / threshold.admissions) * 100)
          : null,
    });
  });

  // Checking for additional requirements (only latest known threshold, first phase)
  const latestThreshold = thresholds.find(
    (threshold) => threshold.year === maxYear && threshold.phase === 1,
  );
  const addidtionalRequirements = latestThreshold?.specialRequirements ?? "";
  // discourage user from providing artificial values
  if (
    fieldData.university.id !== Number(universityId) ||
    fieldData.department.id !== Number(departmentId)
  ) {
    return redirect(`/${fieldData.university.id}/${fieldData.department.id}/${fieldId}`);
  }

  return (
    <>
      <Breadcrumb
        items={[
          {
            name: fieldData.university.name,
            href: `/${fieldData.university.id}`,
          },
          {
            name: fieldData.department.name,
            href: `/${fieldData.university.id}/${fieldData.department.id}`,
          },
          { name: fieldData.name },
        ]}
      />

      <section>
        <div className="flex flex-col lg:flex-row lg:items-start lg:justify-between gap-6">
          <div className="flex items-center gap-5">
            <h1 className="text-foreground">{fieldData.name}</h1>
            <FavoriteButton fieldId={fieldData.id} size="large" />
          </div>

          <div className="lg:w-80">
            <Card className="border-primary/20">
              <CardHeader>
                <CardTitle className="text-foreground">Quick Facts</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <div className="text-sm text-foreground/60">Poziom</div>
                    <div className="font-semibold text-foreground">{fieldData.level}</div>
                  </div>
                  <div>
                    <div className="text-sm text-foreground/60">Semestry</div>
                    <div className="font-semibold text-foreground">{fieldData.duration}</div>
                  </div>
                  <div>
                    <div className="text-sm text-foreground/60">Język</div>
                    <div className="font-semibold text-foreground">{fieldData.language}</div>
                  </div>
                  <div>
                    <div className="text-sm text-foreground/60">Liczba miejsc</div>
                    <div className="font-semibold text-foreground">
                      {latestAdmissionLimit ? latestAdmissionLimit : "Nieznana"}
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </section>

      <section className="mb-12">
        <h2 className="text-foreground mb-6">Kalkulator szans rekrutacji</h2>
        <Card className="border-primary/20">
          <CardHeader>
            <CardTitle className="text-foreground flex items-center gap-2">
              <Calculator className="w-5 h-5" />
              Oblisz swoje szanse
            </CardTitle>
            <CardDescription>
              Podaj swoje wyniki matury a my obliczymy jakie masz szanse dostać się na kierunek:{" "}
              {fieldData.name}
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-6">
            <p className="font-bold text-red-400">TODO: Formularz tutaj</p>
            <div className="bg-primary/5 rounded-lg p-4">
              <h4 className="font-semibold text-foreground mb-2">Dodatkowe wymagania:</h4>
              {addidtionalRequirements ? (
                <p className="">{addidtionalRequirements}</p>
              ) : (
                <p className="text-muted-foreground">
                  Z tego co wiemy, ten kierunek nie ma dodatkowych wymagań rekrutacji
                </p>
              )}
            </div>
          </CardContent>
        </Card>
      </section>

      <section className="mb-12">
        <h2 className="text-foreground mb-6">Informacje o rekrutacji</h2>
        <div className="grid lg:grid-cols-2 gap-6">
          <Card className="border-primary/20 min-w-0">
            <CardHeader>
              <CardTitle className="text-foreground flex items-center gap-2">
                Progi punktowe
              </CardTitle>
              <CardDescription>Wszystkie dostępne historyczne progi</CardDescription>
            </CardHeader>
            <CardContent>
              <CustomLineChart
                data={thresholdsInPhases}
                xDataKey="year"
                yDataKey="threshold"
                showPhases
                tooltipLabel="Punkty:"
              />
            </CardContent>
          </Card>
          <Card className="border-primary/20 min-w-0">
            <CardHeader>
              <CardTitle className="text-foreground flex items-center gap-2">
                Procent przyjętych kandydatów
              </CardTitle>
              <CardDescription>Dostępne dane o odsetku przyjęć kandydatów</CardDescription>
            </CardHeader>
            <CardContent>
              <CustomLineChart
                data={thresholdsInPhases}
                xDataKey="year"
                yDataKey="admissionRate"
                showPhases={false}
                tooltipLabel="% Przyjętych"
              />
            </CardContent>
          </Card>
        </div>
      </section>

      <section>
        <h2 className="text-foreground mb-6">Inne kierunki na tym wydziale</h2>
        <FieldsCarousel fields={departmentFields} />
      </section>
    </>
  );
}
