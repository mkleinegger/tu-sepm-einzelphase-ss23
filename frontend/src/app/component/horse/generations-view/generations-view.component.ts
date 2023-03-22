import {Component, OnInit, Input} from '@angular/core';
import {ToastrService} from 'ngx-toastr';
import {ActivatedRoute} from '@angular/router';
import {HorseService} from 'src/app/service/horse.service';
import {HorseTree} from '../../../dto/horse';
import {Observable} from 'rxjs';
import { filter } from 'rxjs/operators';


@Component({
  selector: 'app-generations-view',
  templateUrl: './generations-view.component.html',
  styleUrls: ['./generations-view.component.scss']
})
export class GenerationsViewComponent implements OnInit {
  tree: HorseTree = {
    id: undefined,
    name: undefined,
    dateOfBirth: undefined,
    sex: undefined,
    generation: undefined,
    mother: undefined,
    father: undefined
  };
  limitOfGenerations: number = 0

  constructor(
    private service: HorseService,
    private notification: ToastrService,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {

    this.route.queryParams.pipe(
      filter(params => params.limitOfGenerations)
    ).subscribe(params => {
      this.limitOfGenerations = params.limitOfGenerations;
    });

      this.route.paramMap.subscribe( paramMap => {
        //id should always be present, because if not this route is not accesible
        const id = paramMap.get('id');
        this.service.getGenerationById(id?.toString(), this.limitOfGenerations).subscribe((response: HorseTree) => {
          this.tree = response;
        });
      });
  }

  reloadFamilyTree(): void {
    this.service.getGenerationById(this.tree.id?.toString() , this.limitOfGenerations).subscribe((response: HorseTree) => {
      this.tree = response;
    });
  }

}
